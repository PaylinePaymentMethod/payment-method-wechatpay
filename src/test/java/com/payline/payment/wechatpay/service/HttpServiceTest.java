package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.nested.TradeState;
import com.payline.payment.wechatpay.bean.request.*;
import com.payline.payment.wechatpay.bean.response.*;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.Converter;
import com.payline.payment.wechatpay.util.ErrorConverter;
import com.payline.payment.wechatpay.util.http.HttpClient;
import com.payline.payment.wechatpay.util.http.StringResponse;
import com.payline.payment.wechatpay.util.security.SignatureUtil;
import com.payline.pmapi.bean.common.FailureCause;
import org.apache.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.openqa.selenium.remote.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class HttpServiceTest {
    @InjectMocks
    @Spy
    private final HttpService service = HttpService.getInstance();
    RequestConfiguration configuration = new RequestConfiguration(
            MockUtils.aContractConfiguration()
            , MockUtils.anEnvironment()
            , MockUtils.aPartnerConfiguration());
    @Mock
    private HttpClient client;

    @Mock
    private Converter converter;

    @Mock
    private SignatureUtil signatureUtil;

    @Mock
    private ErrorConverter errorConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void initHeaders() {
        Header[] headers = service.initHeaders();

        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("Content-Type", headers[0].getName());
        assertEquals("text/xml", headers[0].getValue());
    }

    @Test
    void unifiedOrder() {
        // create Mocks
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn("thisIsASignedXMLMessage").when(signatureUtil).generateSignedXml(any(), any(), any());

        StringResponse sr = new StringResponse(200, null, "a content", null);
        Mockito.doReturn(sr).when(client).post(any(), any(), any());

        UnifiedOrderResponse unifiedOrderResponse = UnifiedOrderResponse.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
            Mockito.doReturn(unifiedOrderResponse).when(converter).xmlToObject(any(), any());

        Mockito.doNothing().when(service).checkSignature(any(), any(), any());
        Mockito.doNothing().when(service).checkResponse(any());

        // call method
        UnifiedOrderRequest request = UnifiedOrderRequest.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .build();
        UnifiedOrderResponse response = service.unifiedOrder(configuration, request);

        // Assertions
        Mockito.verify(converter, Mockito.atLeastOnce()).objectToMap(eq(request));
        Mockito.verify(signatureUtil, Mockito.atLeastOnce()).generateSignedXml(eq(map), eq("key"), any());
        Mockito.verify(converter, Mockito.atLeastOnce()).xmlToObject(eq("a content"), eq(UnifiedOrderResponse.class));
        Mockito.verify(client, Mockito.atLeastOnce()).post(any(), any(), eq("thisIsASignedXMLMessage"));
        Mockito.verify(service, Mockito.atLeastOnce()).checkSignature(any(), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(service, Mockito.atLeastOnce()).checkResponse(eq(unifiedOrderResponse));

        assertEquals(unifiedOrderResponse, response);
    }

    @Test
    void queryOrder() {
        // create Mocks
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn("thisIsASignedXMLMessage").when(signatureUtil).generateSignedXml(any(), any(), any());

        StringResponse sr = new StringResponse(200, null, "a content", null);
        Mockito.doReturn(sr).when(client).post(any(), any(), any());

        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .tradeState(TradeState.SUCCESS)
                .build();
        Mockito.doReturn(queryOrderResponse).when(converter).xmlToObject(any(), any());

        Mockito.doNothing().when(service).checkSignature(any(), any(), any());
        Mockito.doNothing().when(service).checkResponse(any());

        // call method
        QueryOrderRequest request = QueryOrderRequest.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .build();
        QueryOrderResponse response = service.queryOrder(configuration, request);

        // Assertions
        Mockito.verify(converter, Mockito.atLeastOnce()).objectToMap(eq(request));
        Mockito.verify(signatureUtil, Mockito.atLeastOnce()).generateSignedXml(eq(map), eq("key"), any());
        Mockito.verify(converter, Mockito.atLeastOnce()).xmlToObject(eq("a content"), eq(QueryOrderResponse.class));
        Mockito.verify(client, Mockito.atLeastOnce()).post(any(), any(), eq("thisIsASignedXMLMessage"));
        Mockito.verify(service, Mockito.atLeastOnce()).checkSignature(any(), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(service, Mockito.atLeastOnce()).checkResponse(eq(queryOrderResponse));

        assertEquals(queryOrderResponse, response);
    }

    @Test
    void submitRefund() {
        // create Mocks
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn("thisIsASignedXMLMessage").when(signatureUtil).generateSignedXml(any(), any(), any());

        StringResponse sr = new StringResponse(200, null, "a content", null);
        Mockito.doReturn(sr).when(client).post(any(), any(), any());

        SubmitRefundResponse submitRefundResponse = SubmitRefundResponse.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        Mockito.doReturn(submitRefundResponse).when(converter).xmlToObject(any(), any());

        Mockito.doNothing().when(service).checkSignature(any(), any(), any());
        Mockito.doNothing().when(service).checkResponse(any());

        // call method
        SubmitRefundRequest request = SubmitRefundRequest.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .transactionId("123")
                .build();
        SubmitRefundResponse response = service.submitRefund(configuration, request);

        // Assertions
        Mockito.verify(converter, Mockito.atLeastOnce()).objectToMap(eq(request));
        Mockito.verify(signatureUtil, Mockito.atLeastOnce()).generateSignedXml(eq(map), eq("key"), any());
        Mockito.verify(converter, Mockito.atLeastOnce()).xmlToObject(eq("a content"), eq(SubmitRefundResponse.class));
        Mockito.verify(client, Mockito.atLeastOnce()).post(any(), any(), eq("thisIsASignedXMLMessage"));
        Mockito.verify(service, Mockito.atLeastOnce()).checkSignature(any(), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(service, Mockito.atLeastOnce()).checkResponse(eq(submitRefundResponse));

        assertEquals(submitRefundResponse, response);
    }

    @Test
    void queryRefund() {
        // create Mocks
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn("thisIsASignedXMLMessage").when(signatureUtil).generateSignedXml(any(), any(), any());

        StringResponse sr = new StringResponse(200, null, "a content", null);
        Mockito.doReturn(sr).when(client).post(any(), any(), any());

        QueryRefundResponse queryRefundResponse = QueryRefundResponse.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        Mockito.doReturn(queryRefundResponse).when(converter).createQueryResponse(any());

        Mockito.doNothing().when(service).checkSignature(any(), any(), any());
        Mockito.doNothing().when(service).checkResponse(any());

        // call method
        QueryRefundRequest request = QueryRefundRequest.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .build();
        QueryRefundResponse response = service.queryRefund(configuration, request);

        // Assertions
        Mockito.verify(converter, Mockito.atLeastOnce()).objectToMap(eq(request));
        Mockito.verify(signatureUtil, Mockito.atLeastOnce()).generateSignedXml(eq(map), eq("key"), any());
        Mockito.verify(converter, Mockito.atLeastOnce()).createQueryResponse(eq("a content"));
        Mockito.verify(client, Mockito.atLeastOnce()).post(any(), any(), eq("thisIsASignedXMLMessage"));
        Mockito.verify(service, Mockito.atLeastOnce()).checkSignature(any(), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(service, Mockito.atLeastOnce()).checkResponse(eq(queryRefundResponse));

        assertEquals(queryRefundResponse, response);
    }

    @Test
    void downloadTransactionHistory() {
        // create Mocks
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn("thisIsASignedXMLMessage").when(signatureUtil).generateSignedXml(any(), any(), any());

        StringResponse sr = new StringResponse(200, null, "a content", null);
        Mockito.doReturn(sr).when(client).post(any(), any(), any());

        Response weChatPayResponse = UnifiedOrderResponse.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        Mockito.doReturn(weChatPayResponse).when(converter).xmlToObject(any(), any());

        Mockito.doNothing().when(service).checkReturnCode(any());
        Mockito.doNothing().when(service).checkSignature(any(), any(), any());

        // call method
        DownloadTransactionHistoryRequest request = DownloadTransactionHistoryRequest.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .build();
        Response response = service.downloadTransactionHistory(configuration, request);

        // Assertions
        Mockito.verify(converter, Mockito.atLeastOnce()).objectToMap(eq(request));
        Mockito.verify(signatureUtil, Mockito.atLeastOnce()).generateSignedXml(eq(map), eq("key"), any());
        Mockito.verify(converter, Mockito.atLeastOnce()).xmlToObject(eq("a content"), eq(Response.class));
        Mockito.verify(client, Mockito.atLeastOnce()).post(any(), any(), eq("thisIsASignedXMLMessage"));
        Mockito.verify(service, Mockito.atLeastOnce()).checkReturnCode(eq(weChatPayResponse));
        Mockito.verify(service, Mockito.atLeastOnce()).checkSignature(any(), eq("key"), eq(SignType.MD5.getType()));

        assertEquals(weChatPayResponse, response);
    }

    @Test
    void checkReturnCodeOK() {
        Response response = Response.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();

        assertDoesNotThrow(() -> service.checkReturnCode(response));
    }

    @Test
    void checkReturnCodeKO() {
        Response response = Response.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.FAIL)
                .resultCode(Code.SUCCESS)
                .returnMessage("a message")
                .build();

        PluginException e = assertThrows(PluginException.class, () -> service.checkReturnCode(response));
        assertEquals("a message", e.getErrorCode());
        assertEquals(FailureCause.PAYMENT_PARTNER_ERROR, e.getFailureCause());
    }

    @Test
    void checkResultCodeOK() {
        Response response = Response.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();

        assertDoesNotThrow(() -> service.checkResultCode(response));
    }

    @Test
    void checkResultCodeKO() {
        Mockito.doReturn(FailureCause.SESSION_EXPIRED).when(errorConverter).convert(any());

        Response response = Response.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .returnMessage("return message")
                .resultCode(Code.FAIL)
                .errorCode("an error code")
                .errorCodeDescription("error code description")
                .build();

        PluginException e = assertThrows(PluginException.class, () -> service.checkResultCode(response));
        assertEquals("error code description", e.getErrorCode());
        assertEquals(FailureCause.SESSION_EXPIRED, e.getFailureCause());

        Mockito.verify(errorConverter, Mockito.atLeastOnce()).convert(eq("an error code"));
    }

    @Test
    void checkSignatureOK() {
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn(true).when(signatureUtil).isSignatureValid(any(), any(), any());

        Response response = Response.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();


        assertDoesNotThrow(() -> service.checkSignature(converter.objectToMap(response), "a key", SignType.MD5.getType()));
        Mockito.verify(converter, Mockito.atLeastOnce()).objectToMap(eq(response));
        Mockito.verify(signatureUtil, Mockito.atLeastOnce()).isSignatureValid(eq(map), eq("a key"), eq(SignType.MD5.getType()));
    }

    @Test
    void checkSignatureKO() {
        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        Mockito.doReturn(map).when(converter).objectToMap(any());
        Mockito.doReturn(false).when(signatureUtil).isSignatureValid(any(), any(), any());


        Response response = Response.builder()
                .appId("appId")
                .merchantId("merchantId")
                .subMerchantId("subMerchantId")
                .nonceStr("123456")
                .signType(SignType.MD5.getType())
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        String signType = SignType.MD5.getType();
        PluginException e = assertThrows(PluginException.class, () -> service.checkSignature(map, "a key", signType));

        assertEquals("Invalid signature", e.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, e.getFailureCause());
    }
}