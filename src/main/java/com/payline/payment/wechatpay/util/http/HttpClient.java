package com.payline.payment.wechatpay.util.http;


import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.payment.wechatpay.util.properties.ConfigProperties;
import com.payline.pmapi.bean.common.FailureCause;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicBoolean;
@Log4j2
public class HttpClient {
    protected ConfigProperties config = ConfigProperties.getInstance();

    /**
     * Has this class been initialized with partner configuration ?
     */
    protected AtomicBoolean initialized = new AtomicBoolean();


    /**
     * The number of time the client must retry to send the request if it doesn't obtain a response.
     */
    private int retries;

    private org.apache.http.client.HttpClient client;

    // --- Singleton Holder pattern + initialization BEGIN

    private HttpClient() {
        int connectionRequestTimeout;
        int connectTimeout;
        int socketTimeout;
        try {
            // request config timeouts (in seconds)
            ConfigProperties configProperties = ConfigProperties.getInstance();
            connectionRequestTimeout = Integer.parseInt(configProperties.get("http.connectionRequestTimeout"));
            connectTimeout = Integer.parseInt(configProperties.get("http.connectTimeout"));
            socketTimeout = Integer.parseInt(configProperties.get("http.socketTimeout"));

            // retries
            this.retries = Integer.parseInt(configProperties.get("http.retries"));
        } catch (NumberFormatException e) {
            throw new PluginException("plugin error: http.* properties must be integers", e);
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout * 1000)
                .setConnectTimeout(connectTimeout * 1000)
                .setSocketTimeout(socketTimeout * 1000)
                .build();

        // instantiate Apache HTTP client
        this.client = HttpClientBuilder.create()
                .useSystemProperties()
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(new SSLConnectionSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), SSLConnectionSocketFactory.getDefaultHostnameVerifier()))
                .build();

    }

    private static class Holder {
        private static final HttpClient instance = new HttpClient();
    }


    public static HttpClient getInstance() {
        return Holder.instance;
    }
    // --- Singleton Holder pattern + initialization END


    /**
     * Initialize the instance.
     *
     * @param tokenEndpointUrl the full URL of the endpoint that delivers access tokens
     */
    public void init(RequestConfiguration configuration) {
        if (this.initialized.compareAndSet(false, true)) {
            // Set the token endpoint URL

            // Retrieve config properties
            int connectionRequestTimeout;
            int connectTimeout;
            int socketTimeout;
            try {
                // request config timeouts (in seconds)
                connectionRequestTimeout = Integer.parseInt(config.get("http.connectionRequestTimeout"));
                connectTimeout = Integer.parseInt(config.get("http.connectTimeout"));
                socketTimeout = Integer.parseInt(config.get("http.socketTimeout"));

                // number of retry attempts
                this.retries = Integer.parseInt(config.get("http.retries"));
            } catch (NumberFormatException e) {
                throw new PluginException("plugin error: http.* properties must be integers", e);
            }

            // Create RequestConfig
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(connectionRequestTimeout * 1000)
                    .setConnectTimeout(connectTimeout * 1000)
                    .setSocketTimeout(socketTimeout * 1000)
                    .build();


            char[] password = configuration.getContractConfiguration()
                    .getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue().toCharArray();

            File file = new File(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.CERTIFICATE));
            try(InputStream certStream = new FileInputStream(file)){
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(certStream, password);

                // 实例化密钥库 & 初始化密钥工厂
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, password);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

                SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                        sslContext,
                        new String[]{"TLSv1"},
                        null,
                        new DefaultHostnameVerifier());

                BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
                        RegistryBuilder.<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                                .register("https", sslConnectionSocketFactory)
                                .build(),
                        null,
                        null,
                        null
                );

                // Instantiate Apache HTTP client
                this.client = HttpClientBuilder.create()
                        .setConnectionManager(connManager)
                        .setDefaultRequestConfig(requestConfig)
                        .setSSLSocketFactory(new SSLConnectionSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), SSLConnectionSocketFactory.getDefaultHostnameVerifier()))
                        .build();

            } catch (CertificateException e) {
                throw new PluginException("Plugin error: Certificate error", e);
            } catch (NoSuchAlgorithmException e) {
                throw new PluginException("Plugin error: Invalid algorithm", e);
            } catch (UnrecoverableKeyException e) {
                throw new PluginException("Plugin error: Invalid Key", e);
            } catch (KeyStoreException e) {
                throw new PluginException("Plugin error: Keystore error", e);
            } catch (KeyManagementException e) {
                throw new PluginException("Plugin error: Key error", e);
            } catch (IOException e) {
                throw new PluginException("Plugin error: Certificate loading error", e);
            }
        }
    }



    /**
     * Send the request, with a retry system in case the client does not obtain a proper response from the server.
     *
     * @param httpRequest The request to send.
     * @return The response converted as a {@link StringResponse}.
     * @throws PluginException If an error repeatedly occurs and no proper response is obtained.
     */
    public StringResponse execute(HttpRequestBase httpRequest) {
        StringResponse strResponse = null;
        int attempts = 1;

        while (strResponse == null && attempts <= this.retries) {
            log.info("Start call to partner API [{} {}] (attempt {})", httpRequest.getMethod(), httpRequest.getURI(), attempts);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.client.execute(httpRequest)) {
                strResponse = StringResponse.fromHttpResponse(httpResponse);
            } catch (IOException e) {
                log.error("An error occurred during the HTTP call :", e);
                strResponse = null;
            } finally {
                attempts++;
            }
        }

        if (strResponse == null) {
            throw new PluginException("Failed to contact the partner API", FailureCause.COMMUNICATION_ERROR);
        }
        log.info("APIResponseError obtained from partner API [{} {}]", strResponse.getStatusCode(), strResponse.getStatusMessage());
        return strResponse;
    }

    /**
     * Manage Get API call
     *
     * @param uri     the url to call
     * @param headers header(s) of the request
     * @return
     */
    public StringResponse get(URI uri, Header[] headers) {
        final HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeaders(headers);

        // Execute request
        return this.execute(httpGet);
    }

    /**
     * Manage Post API call
     *
     * @param uri     the url to call
     * @param headers header(s) of the request
     * @param body    body of the request
     * @return
     */
    public StringResponse post(URI uri, Header[] headers, String body) {
        final HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeaders(headers);

        // Body
        if (body != null) {
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        }

        // Execute request
        return this.execute(httpPost);
    }
}