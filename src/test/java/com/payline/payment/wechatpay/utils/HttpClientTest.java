package com.payline.payment.wechatpay.utils;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.http.HttpClient;
import com.payline.payment.wechatpay.util.http.StringResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;

import static com.payline.payment.wechatpay.utils.HttpTestUtils.mockHttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpClientTest {

    @InjectMocks
    @Spy
    private HttpClient httpClient;
    @Mock
    private CloseableHttpClient http;

    @BeforeEach
    void setup() {
        // Init tested instance and inject mocks
        httpClient = HttpClient.getInstance();
        MockitoAnnotations.initMocks(this);
    }
    /**------------------------------------------------------------------------------------------------------------------*/
    @Test
    void execute_nominal() throws IOException {
        // given: a properly formatted request, which gets a proper response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        int expectedStatusCode = 200;
        String expectedStatusMessage = "OK";
        String expectedContent = "{\"content\":\"fake\"}";
        doReturn(mockHttpResponse(expectedStatusCode, expectedStatusMessage, expectedContent, null))
                .when(http).execute(request);

        // when: sending the request

        StringResponse stringResponse = httpClient.execute(request);

        // then: the content of the StringResponse reflects the content of the HTTP response
        assertNotNull(stringResponse);
        assertEquals(expectedStatusCode, stringResponse.getStatusCode());
        assertEquals(expectedStatusMessage, stringResponse.getStatusMessage());
        assertEquals(expectedContent, stringResponse.getContent());
    }
    /**------------------------------------------------------------------------------------------------------------------*/
    @Test
    void execute_retry() throws IOException {
        // given: the first 2 requests end up in timeout, the third request gets a response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        when(http.execute(request))
                .thenThrow(ConnectTimeoutException.class)
                .thenThrow(ConnectTimeoutException.class)
                .thenReturn(mockHttpResponse(200, "OK", "content", null));

        // when: sending the request
        StringResponse stringResponse = httpClient.execute(request);

        // then: the client finally gets the response
        assertNotNull(stringResponse);
    }
    /**------------------------------------------------------------------------------------------------------------------*/
    @Test
    void execute_retryFail() throws IOException {
        // given: a request which always gets an exception
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        doThrow(IOException.class).when(http).execute(request);

        // when: sending the request, a PluginException is thrown
        assertThrows(PluginException.class, () -> httpClient.execute(request));
    }
    /**------------------------------------------------------------------------------------------------------------------*/
    @Test
    void execute_invalidResponse() throws IOException {
        // given: a request that gets an invalid response (null)
        HttpGet request = new HttpGet("http://domain.test.fr/malfunctioning-endpoint");
        doReturn(null).when(http).execute(request);

        // when: sending the request, a PluginException is thrown
        assertThrows(PluginException.class, () -> httpClient.execute(request));
    }
    /**------------------------------------------------------------------------------------------------------------------*/
}
