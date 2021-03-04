package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.nested.TradeState;
import com.payline.payment.wechatpay.bean.response.NotificationMessage;
import com.payline.payment.wechatpay.bean.response.QueryOrderResponse;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.payment.wechatpay.util.Converter;
import com.payline.payment.wechatpay.util.XMLService;
import com.payline.payment.wechatpay.util.security.SignatureUtil;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class NotificationServiceImplTest {
    @InjectMocks
    NotificationServiceImpl service = new NotificationServiceImpl();

    @Mock
    XMLService xmlService;
    @Mock
    Converter converter;

    @Mock
    HttpService httpService;

    @Mock
    SignatureUtil signatureUtil;


    String message = "<xml>" +
            "<appid><![CDATA[wx2421b1c4370ec43b]]></appid>" +
            "<attach><![CDATA[PaymentTesting]]></attach>" +
            "<bank_type><![CDATA[CFT]]></bank_type>" +
            "<fee_type><![CDATA[CNY]]></fee_type>" +
            "<is_subscribe><![CDATA[Y]]></is_subscribe>" +
            "<mch_id><![CDATA[10000100]]></mch_id>" +
            "<nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>" +
            "<openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>" +
            "<out_trade_no><![CDATA[1409811653]]></out_trade_no>" +
            "<result_code><![CDATA[SUCCESS]]></result_code>" +
            "<return_code><![CDATA[SUCCESS]]></return_code>" +
            "<sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>" +
            "<sub_mch_id><![CDATA[10000100]]></sub_mch_id>" +
            "<time_end><![CDATA[20140903131540]]></time_end>" +
            "<total_fee>1</total_fee>" +
            "<trade_type><![CDATA[JSAPI]]></trade_type>" +
            "<transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id>" +
            "</xml>";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse() {
        // create mocks
        Map<String,String> map = new HashMap<>();
        map.put("1","2");
        Mockito.doReturn(map).when(xmlService).xmlToMap(any());

        Mockito.doReturn(true).when(signatureUtil).isSignatureValid(any(), any(), any());

        NotificationMessage notificationMessage = NotificationMessage.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        Mockito.doReturn(notificationMessage).when(converter).mapToObject(any(), any());

        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .tradeState(TradeState.SUCCESS)
                .transactionId("123456")
                .build();
        Mockito.doReturn(queryOrderResponse).when(httpService).queryOrder(any(), any());

        // call method
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)))
                .build();
        NotificationResponse response = service.parse(request);

        // assertions
        assertNotNull(response);
        assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        PaymentResponse paymentResponse = paymentResponseByNotificationResponse.getPaymentResponse();

        assertEquals(PaymentResponseSuccess.class, paymentResponse.getClass());
        PaymentResponseSuccess responseSuccess = (PaymentResponseSuccess) paymentResponse;
        assertEquals("123456", responseSuccess.getPartnerTransactionId());
        assertEquals("SUCCESS", responseSuccess.getStatusCode());
        assertEquals(EmptyTransactionDetails.class, responseSuccess.getTransactionDetails().getClass());

        Mockito.verify(xmlService, Mockito.atLeastOnce()).xmlToMap(eq(message));
        Mockito.verify(converter, Mockito.atLeastOnce()).mapToObject(eq(map), eq(NotificationMessage.class));

        Mockito.verify(signatureUtil).isSignatureValid(eq(map), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(httpService, Mockito.atLeastOnce()).queryOrder(any(), any());
    }

    @Test
    void parseNOTPAY() {
        // create mocks
        Map<String,String> map = new HashMap<>();
        map.put("1","2");
        Mockito.doReturn(map).when(xmlService).xmlToMap(any());

        Mockito.doReturn(true).when(signatureUtil).isSignatureValid(any(), any(), any());

        NotificationMessage notificationMessage = NotificationMessage.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        Mockito.doReturn(notificationMessage).when(converter).mapToObject(any(), any());

        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .tradeState(TradeState.NOTPAY)
                .errorCode("an error code")
                .transactionId("123456")
                .build();
        Mockito.doReturn(queryOrderResponse).when(httpService).queryOrder(any(), any());

        // call method
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)))
                .build();
        NotificationResponse response = service.parse(request);

        // assertions
        assertNotNull(response);
        assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        PaymentResponse paymentResponse = paymentResponseByNotificationResponse.getPaymentResponse();

        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("123456", paymentResponseFailure.getPartnerTransactionId());
        assertEquals("an error code", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.PARTNER_UNKNOWN_ERROR, paymentResponseFailure.getFailureCause());
        assertEquals(EmptyTransactionDetails.class, paymentResponseFailure.getTransactionDetails().getClass());

        Mockito.verify(xmlService, Mockito.atLeastOnce()).xmlToMap(eq(message));
        Mockito.verify(converter, Mockito.atLeastOnce()).mapToObject(eq(map), eq(NotificationMessage.class));

        Mockito.verify(signatureUtil).isSignatureValid(eq(map), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(httpService, Mockito.atLeastOnce()).queryOrder(any(), any());
    }

    // todo tester avec signature invalide
    @Test
    void parseInvalidSignature() {
        // create mocks
        Map<String,String> map = new HashMap<>();
        map.put("1","2");
        Mockito.doReturn(map).when(xmlService).xmlToMap(any());

        Mockito.doReturn(true).when(signatureUtil).isSignatureValid(any(), any(), any());

        NotificationMessage notificationMessage = NotificationMessage.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .build();
        Mockito.doReturn(notificationMessage).when(converter).mapToObject(any(), any());

        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .tradeState(TradeState.SUCCESS)
                .transactionId("123456")
                .build();
        Mockito.doReturn(queryOrderResponse).when(httpService).queryOrder(any(), any());

        // call method
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)))
                .build();
        NotificationResponse response = service.parse(request);

        // assertions
        assertNotNull(response);
        assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        PaymentResponse paymentResponse = paymentResponseByNotificationResponse.getPaymentResponse();

        assertEquals(PaymentResponseSuccess.class, paymentResponse.getClass());
        PaymentResponseSuccess responseSuccess = (PaymentResponseSuccess) paymentResponse;
        assertEquals("123456", responseSuccess.getPartnerTransactionId());
        assertEquals("SUCCESS", responseSuccess.getStatusCode());
        assertEquals(EmptyTransactionDetails.class, responseSuccess.getTransactionDetails().getClass());

        Mockito.verify(xmlService, Mockito.atLeastOnce()).xmlToMap(eq(message));
        Mockito.verify(converter, Mockito.atLeastOnce()).mapToObject(eq(map), eq(NotificationMessage.class));

        Mockito.verify(signatureUtil).isSignatureValid(eq(map), eq("key"), eq(SignType.MD5.getType()));
        Mockito.verify(httpService, Mockito.atLeastOnce()).queryOrder(any(), any());
    }

    @Test
    void parsePluginException() {
        // create mocks
        Mockito.doThrow(new PluginException("foo", FailureCause.INTERNAL_ERROR)).when(xmlService).xmlToMap(any());

        // call method
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)))
                .build();
        NotificationResponse response = service.parse(request);

        // assertions
        assertNotNull(response);
        assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        PaymentResponse paymentResponse = paymentResponseByNotificationResponse.getPaymentResponse();

        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("UNKNOWN", paymentResponseFailure.getPartnerTransactionId());
        assertEquals("foo", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
    }

    @Test
    void parseRuntimeException() {
        // create mocks
        Mockito.doThrow(new NullPointerException("foo")).when(xmlService).xmlToMap(any());

        // call method
        NotificationRequest request = MockUtils.aNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)))
                .build();
        NotificationResponse response = service.parse(request);

        // assertions
        assertNotNull(response);
        assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        PaymentResponse paymentResponse = paymentResponseByNotificationResponse.getPaymentResponse();

        assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("UNKNOWN", paymentResponseFailure.getPartnerTransactionId());
        assertEquals("plugin error: NullPointerException: foo", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
    }


    @Test
    void notifyTransactionStatus() {
        assertDoesNotThrow(() -> service.notifyTransactionStatus(Mockito.mock(NotifyTransactionStatusRequest.class)));
    }
}