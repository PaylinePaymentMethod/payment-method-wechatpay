package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.request.*;
import com.payline.payment.wechatpay.bean.response.*;
import com.payline.payment.wechatpay.exception.InvalidDataException;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.Converter;
import com.payline.payment.wechatpay.util.ErrorConverter;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.payment.wechatpay.util.http.HttpClient;
import com.payline.payment.wechatpay.util.http.StringResponse;
import com.payline.payment.wechatpay.util.security.SignatureUtil;
import com.payline.pmapi.bean.common.FailureCause;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class HttpService {
    private static final String INVALID_URL_MESSAGE = "invalid URL";
    private static final String INVALID_URL_LOG_MESSAGE = "invalid URL for property: {}";
    private HttpClient client = HttpClient.getInstance();
    private Converter converter = Converter.getInstance();
    private SignatureUtil signatureUtil = SignatureUtil.getInstance();
    private ErrorConverter errorConverter = ErrorConverter.getInstance();

    public static HttpService getInstance() {
        return Holder.instance;
    }

    protected Header[] initHeaders() {

         return new Header[]{
                new BasicHeader("Content-Type", "text/xml")
        };
    }
    // --- Singleton Holder pattern + initialization END

    public UnifiedOrderResponse unifiedOrder(RequestConfiguration configuration, UnifiedOrderRequest request) {
        try {
            // get needed data
            String key = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.KEY);

            URI uri = new URI(configuration
                    .getPartnerConfiguration()
                    .getProperty(PartnerConfigurationKeys.UNIFIED_ORDER_URL)
            );
            Header[] headers = initHeaders();
            Map<String, String> map = converter.objectToMap(request);
            String body = signatureUtil.generateSignedXml(map, key, request.getSignType());

            // does the call
            StringResponse response = client.post(uri, headers, body);
            UnifiedOrderResponse unifiedOrderResponse = converter.xmlToObject(response.getContent(), UnifiedOrderResponse.class);

            // check signature
            checkSignature(converter.xmlToMap(response.getContent()),key,request.getSignType());
            // check response
            checkResponse(unifiedOrderResponse);

            return unifiedOrderResponse;
        } catch (URISyntaxException e) {
            log.error(INVALID_URL_LOG_MESSAGE, PartnerConfigurationKeys.UNIFIED_ORDER_URL);
            throw new InvalidDataException(INVALID_URL_MESSAGE);
        }
    }

    public QueryOrderResponse queryOrder(RequestConfiguration configuration, QueryOrderRequest request) {
        try {
            // get needed data
            String key = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.KEY);

            URI uri = new URI(configuration
                    .getPartnerConfiguration()
                    .getProperty(PartnerConfigurationKeys.QUERY_ORDER_URL)
            );
            Header[] headers = initHeaders();


            Map<String, String> map = converter.objectToMap(request);
            String body = signatureUtil.generateSignedXml(map, key, request.getSignType());

            // does the call
            StringResponse response = client.post(uri, headers, body);
            QueryOrderResponse queryOrderResponse = converter.xmlToObject(response.getContent(), QueryOrderResponse.class);

            // check signature
            checkSignature(converter.xmlToMap(response.getContent()),key,request.getSignType());
            // check response
            checkResponse(queryOrderResponse);

            return queryOrderResponse;
        } catch (URISyntaxException e) {
            log.error(INVALID_URL_LOG_MESSAGE, PartnerConfigurationKeys.QUERY_ORDER_URL);
            throw new InvalidDataException(INVALID_URL_MESSAGE);
        }
    }

    public SubmitRefundResponse submitRefund(RequestConfiguration configuration, SubmitRefundRequest request) {
        try {
            // get needed data
            String key = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.KEY);

            client.init(configuration);

            URI uri = new URI(configuration
                    .getPartnerConfiguration()
                    .getProperty(PartnerConfigurationKeys.SUBMIT_REFUND_URL)
            );
            Header[] headers = initHeaders();
            Map<String, String> map = converter.objectToMap(request);
            String body = signatureUtil.generateSignedXml(map, key, request.getSignType());

            // does the call
            StringResponse response = client.post(uri, headers, body);
            SubmitRefundResponse submitRefundResponse = converter.xmlToObject(response.getContent(), SubmitRefundResponse.class);

            // check signature
            checkSignature(converter.xmlToMap(response.getContent()), key, request.getSignType());
            // check response
            checkResponse(submitRefundResponse);

            return submitRefundResponse;
        } catch (URISyntaxException e) {
            log.error(INVALID_URL_LOG_MESSAGE, PartnerConfigurationKeys.SUBMIT_REFUND_URL);
            throw new InvalidDataException(INVALID_URL_MESSAGE);
        }
    }

    public QueryRefundResponse queryRefund(RequestConfiguration configuration, QueryRefundRequest request) {
        try {
            // get needed data
            String key = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.KEY);

            URI uri = new URI(configuration
                    .getPartnerConfiguration()
                    .getProperty(PartnerConfigurationKeys.QUERY_REFUND_URL)
            );
            Header[] headers = initHeaders();
            Map<String, String> map = converter.objectToMap(request);
            String body = signatureUtil.generateSignedXml(map, key, request.getSignType());

            // does the call
            StringResponse response = client.post(uri, headers, body);
            QueryRefundResponse queryRefundResponse = converter.createQueryResponse(response.getContent());

            // check signature
            checkSignature(converter.xmlToMap(response.getContent()),key,request.getSignType());
            // check response
            checkResponse(queryRefundResponse);

            return queryRefundResponse;
        } catch (URISyntaxException e) {
            log.error(INVALID_URL_LOG_MESSAGE, PartnerConfigurationKeys.QUERY_REFUND_URL);
            throw new InvalidDataException(INVALID_URL_MESSAGE);
        }
    }

    public Response downloadTransactionHistory(RequestConfiguration configuration, DownloadTransactionHistoryRequest request) {
        try {
            // get needed data
            String key = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.KEY);

            URI uri = new URI(configuration
                    .getPartnerConfiguration()
                    .getProperty(PartnerConfigurationKeys.DOWNLOAD_TRANSACTIONS_URL)
            );
            Header[] headers = initHeaders();
            Map<String, String> map = converter.objectToMap(request);
            String body = signatureUtil.generateSignedXml(map, key, request.getSignType());

            // does the call
            StringResponse sResponse = client.post(uri, headers, body);
            Response response = converter.xmlToObject(sResponse.getContent(), Response.class);

            // check signature
            checkSignature(converter.xmlToMap(sResponse.getContent()), key, request.getSignType());
            // check response
            checkReturnCode(response);

            return response;
        } catch (URISyntaxException e) {
            log.error(INVALID_URL_LOG_MESSAGE, PartnerConfigurationKeys.DOWNLOAD_TRANSACTIONS_URL);
            throw new InvalidDataException(INVALID_URL_MESSAGE);
        }
    }

    void checkResponse(Response response) {
        checkReturnCode(response);
        checkResultCode(response);
    }

    void checkReturnCode(Response response) {
        Code returnCode = response.getReturnCode();
        if (returnCode.equals(Code.FAIL)) {
            log.error("return_code FAIL in response {}", response.toString());
            throw new PluginException(response.getReturnMessage(), FailureCause.PAYMENT_PARTNER_ERROR);
        }
    }

    void checkSignature(Map<String, String> response, String key, String signType) {
        if (!signatureUtil.isSignatureValid(response, key, signType)) {
            log.error("Invalid sign value in XML: {}", response.toString());
            throw new PluginException("Invalid signature", FailureCause.INVALID_DATA);
        }
    }

    void checkResultCode(Response response) {
        Code resultCode = response.getResultCode();
        if (Code.FAIL.equals(resultCode)) {
            log.error("result_code FAIL in response {}", response.toString());
            throw new PluginException(response.getErrorCodeDescription()
                    , errorConverter.convert(response.getErrorCode()));
        }
    }

    private static class Holder {
        private static final HttpService instance = new HttpService();
    }
}
