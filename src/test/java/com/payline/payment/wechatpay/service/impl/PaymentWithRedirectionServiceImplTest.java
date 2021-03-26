package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.*;
import com.payline.payment.wechatpay.bean.response.QueryOrderResponse;
import com.payline.payment.wechatpay.bean.response.QueryRefundResponse;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static com.payline.payment.wechatpay.MockUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class PaymentWithRedirectionServiceImplTest {

    @InjectMocks
    @Spy
    PaymentWithRedirectionServiceImpl service = new PaymentWithRedirectionServiceImpl();
    @Mock
    private HttpService httpService;

    RequestConfiguration configuration = new RequestConfiguration(
            MockUtils.aContractConfiguration()
            , MockUtils.anEnvironment()
            , MockUtils.aPartnerConfiguration());

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void handleSessionExpiredPaymentOK() {
        // create Mocks
        PaymentResponseSuccess paymentResponseSuccess = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                .aPaymentResponseSuccess()
                .withPartnerTransactionId("foo")
                .withStatusCode("a status code")
                .withTransactionDetails(new EmptyTransactionDetails())
                .build();
        Mockito.doReturn(paymentResponseSuccess).when(service).getPaymentStatus(any(), any());

        // call method
        TransactionStatusRequest request = MockUtils.aPaylineTransactionStatusRequestBuilder().build();
        PaymentResponse paymentResponse = service.handleSessionExpired(request);

        // assertions
        assertEquals(paymentResponseSuccess, paymentResponse);

        Mockito.verify(service, Mockito.atLeastOnce()).getPaymentStatus(any(), any());
    }

    @Test
    void handleSessionExpiredRefundOK() {
        // create Mocks
        PaymentResponseSuccess paymentResponseSuccess = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                .aPaymentResponseSuccess()
                .withPartnerTransactionId("foo")
                .withStatusCode("a status code")
                .withTransactionDetails(new EmptyTransactionDetails())
                .build();
        Mockito.doReturn(paymentResponseSuccess).when(service).fetchRefundStatus(any(), any());


        // call method
        TransactionStatusRequest request = MockUtils.aPaylineTransactionStatusRequestBuilder()
                .withTransactionId("REFUND1234556788")
                .build();
        PaymentResponse paymentResponse = service.handleSessionExpired(request);

        // assertions
        assertEquals(paymentResponseSuccess, paymentResponse);

        Mockito.verify(service, Mockito.atLeastOnce()).fetchRefundStatus(any(), any());
    }

    @Test
    void paymentRequest_PluginException() {
        Mockito.doThrow(new PluginException("foo")).when(service).getPaymentStatus(any(), any());

        // when: sending the request, a PluginException is thrown
        PaymentResponse paymentResponse = service.handleSessionExpired(MockUtils.aPaylineTransactionStatusRequest());

        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, ((PaymentResponseFailure) paymentResponse).getFailureCause());
    }

    @Test
    void paymentRequest_RunTimeException() {
        Mockito.doThrow(new RuntimeException("foo")).when(service).getPaymentStatus(any(), any());

        // when: sending the request, a PluginException is thrown
        PaymentResponse paymentResponse = service.handleSessionExpired(MockUtils.aPaylineTransactionStatusRequest());

        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, ((PaymentResponseFailure) paymentResponse).getFailureCause());
    }

    @Test
    void getPaymentStatus_SUCCESS() {
        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .returnCode(Code.SUCCESS)
                .appId("appId")
                .merchantId("merchantId")
                .errorCode("errorCode")
                .nonceStr("nonceStr")
                .errorCodeDescription("errorCodeDescription")
                .resultCode(Code.SUCCESS)
                .returnMessage("returnMessage")
                .sign("sign")
                .signType(SignType.HMACSHA256.getType())
                .subAppId("subAppId")
                .subMerchantId("subMerchantId")
                .tradeState(TradeState.SUCCESS)
                .transactionId("transactionId")
                .build();

        Mockito.doReturn(queryOrderResponse).when(httpService).queryOrder(any(), any());

        PaymentResponse response = service.handleSessionExpired(MockUtils.aPaylineTransactionStatusRequest());

        Assertions.assertEquals(PaymentResponseSuccess.class, response.getClass());
        Assertions.assertEquals(queryOrderResponse.getTransactionId(), ((PaymentResponseSuccess) response).getPartnerTransactionId());
        Assertions.assertEquals(queryOrderResponse.getTradeState().name(), ((PaymentResponseSuccess) response).getStatusCode());
    }

    @Test
    void getPaymentStatus_NOTPAY() {
        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .returnCode(Code.SUCCESS)
                .appId("appId")
                .merchantId("merchantId")
                .errorCode("errorCode")
                .nonceStr("nonceStr")
                .errorCodeDescription("errorCodeDescription")
                .resultCode(Code.SUCCESS)
                .returnMessage("returnMessage")
                .sign("sign")
                .signType(SignType.HMACSHA256.getType())
                .subAppId("subAppId")
                .subMerchantId("subMerchantId")
                .tradeState(TradeState.NOTPAY)
                .transactionId("transactionId")
                .build();

        Mockito.doReturn(queryOrderResponse).when(httpService).queryOrder(any(), any());

        PaymentResponse response = service.handleSessionExpired(MockUtils.aPaylineTransactionStatusRequest());

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assertions.assertEquals(queryOrderResponse.getTransactionId(), ((PaymentResponseFailure) response).getPartnerTransactionId());
        Assertions.assertEquals(queryOrderResponse.getErrorCode(), ((PaymentResponseFailure) response).getErrorCode());
        Assertions.assertEquals(FailureCause.CANCEL, ((PaymentResponseFailure) response).getFailureCause());
    }

    @Test
    void getPaymentStatus_INVALID_DATA() {
        QueryOrderResponse queryOrderResponse = QueryOrderResponse.builder()
                .returnCode(Code.SUCCESS)
                .appId("appId")
                .merchantId("merchantId")
                .errorCode("errorCode")
                .nonceStr("nonceStr")
                .errorCodeDescription("errorCodeDescription")
                .resultCode(Code.SUCCESS)
                .returnMessage("returnMessage")
                .sign("sign")
                .signType(SignType.HMACSHA256.getType())
                .subAppId("subAppId")
                .subMerchantId("subMerchantId")
                .tradeState(TradeState.REFUND)
                .transactionId("transactionId")
                .build();

        Mockito.doReturn(queryOrderResponse).when(httpService).queryOrder(any(), any());

        PaymentResponse response = service.handleSessionExpired(MockUtils.aPaylineTransactionStatusRequest());

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assertions.assertEquals(queryOrderResponse.getTransactionId(), ((PaymentResponseFailure) response).getPartnerTransactionId());
        Assertions.assertEquals(queryOrderResponse.getErrorCode(), ((PaymentResponseFailure) response).getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) response).getFailureCause());
    }

    @Test
    void getRefundStatus_SUCCESS(){
        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(TRANSACTION_ID).refundStatus(RefundStatus.SUCCESS).build());

        QueryRefundResponse queryRefundResponse = QueryRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refunds(refunds)
                .build();

        Mockito.doReturn(queryRefundResponse).when(httpService).queryRefund(any(), any());
        TransactionStatusRequest transactionStatusRequest = MockUtils.aPaylineTransactionStatusRequestBuilder().build();
        PaymentResponse response = service.fetchRefundStatus(transactionStatusRequest, configuration);

        Assertions.assertEquals(PaymentResponseSuccess.class, response.getClass());
        Assertions.assertEquals(TRANSACTION_ID, ((PaymentResponseSuccess) response).getPartnerTransactionId());
        Assertions.assertEquals("SUCCESS", ((PaymentResponseSuccess) response).getStatusCode());
    }

    @Test
    void getRefundStatus_PROCESSING(){
        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(TRANSACTION_ID).refundStatus(RefundStatus.PROCESSING).build());

        QueryRefundResponse queryRefundResponse = QueryRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refunds(refunds)
                .build();

        Mockito.doReturn(queryRefundResponse).when(httpService).queryRefund(any(), any());
        TransactionStatusRequest transactionStatusRequest = MockUtils.aPaylineTransactionStatusRequestBuilder().build();
        PaymentResponse response = service.fetchRefundStatus(transactionStatusRequest, configuration);

        Assertions.assertEquals(PaymentResponseSuccess.class, response.getClass());
        Assertions.assertEquals(TRANSACTION_ID, ((PaymentResponseSuccess) response).getPartnerTransactionId());
        Assertions.assertEquals("PENDING", ((PaymentResponseSuccess) response).getStatusCode());
    }

    @Test
    void getRefundStatus_REFUNDCLOSE(){
        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(TRANSACTION_ID).refundStatus(RefundStatus.REFUNDCLOSE).build());

        QueryRefundResponse queryRefundResponse = QueryRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refunds(refunds)
                .build();

        Mockito.doReturn(queryRefundResponse).when(httpService).queryRefund(any(), any());
        TransactionStatusRequest transactionStatusRequest = MockUtils.aPaylineTransactionStatusRequestBuilder().build();
        PaymentResponse response = service.fetchRefundStatus(transactionStatusRequest, configuration);

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assertions.assertEquals(TRANSACTION_ID, ((PaymentResponseFailure) response).getPartnerTransactionId());
        Assertions.assertEquals("REFUNDCLOSE", ((PaymentResponseFailure) response).getErrorCode());
        Assertions.assertEquals(FailureCause.REFUSED, ((PaymentResponseFailure) response).getFailureCause());
    }
}