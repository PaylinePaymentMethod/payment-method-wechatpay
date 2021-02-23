package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.nested.Refund;
import com.payline.payment.wechatpay.bean.nested.RefundStatus;
import com.payline.payment.wechatpay.bean.response.QueryRefundResponse;
import com.payline.payment.wechatpay.bean.response.SubmitRefundResponse;
import com.payline.payment.wechatpay.exception.InvalidDataException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class RefundServiceImplTest {
    String refundId = "123456789";

    @InjectMocks
    private RefundServiceImpl service = new RefundServiceImpl();

    @Mock
    private HttpService httpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void refundRequestNominal() {
        // create Mocks
        SubmitRefundResponse submitRefundResponse = SubmitRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refundId(refundId)
                .build();
        Mockito.doReturn(submitRefundResponse).when(httpService).submitRefund(any(), any());

        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(refundId).refundStatus(RefundStatus.SUCCESS).build());

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


        // call method
        RefundRequest refundRequest = MockUtils.aRefundRequestBuilder().build();
        RefundResponse refundResponse = service.refundRequest(refundRequest);

        // assertions
        assertNotNull(refundResponse);
        assertEquals(RefundResponseSuccess.class, refundResponse.getClass());
        RefundResponseSuccess refundResponseSuccess = (RefundResponseSuccess) refundResponse;
        assertEquals(refundId, refundResponseSuccess.getPartnerTransactionId());
        assertEquals("SUCCESS", refundResponseSuccess.getStatusCode());
    }

    @Test
    void refundRequestProccessing() {
        // create Mocks
        SubmitRefundResponse submitRefundResponse = SubmitRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refundId(refundId)
                .build();
        Mockito.doReturn(submitRefundResponse).when(httpService).submitRefund(any(), any());

        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(refundId).refundStatus(RefundStatus.PROCESSING).build());

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


        // call method
        RefundRequest refundRequest = MockUtils.aRefundRequestBuilder().build();
        RefundResponse refundResponse = service.refundRequest(refundRequest);

        // assertions
        assertNotNull(refundResponse);
        assertEquals(RefundResponseSuccess.class, refundResponse.getClass());
        RefundResponseSuccess refundResponseSuccess = (RefundResponseSuccess) refundResponse;
        assertEquals(refundId, refundResponseSuccess.getPartnerTransactionId());
        assertEquals("PENDING", refundResponseSuccess.getStatusCode());
    }

    @Test
    void refundRequestClose() {
        // create Mocks
        SubmitRefundResponse submitRefundResponse = SubmitRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refundId(refundId)
                .build();
        Mockito.doReturn(submitRefundResponse).when(httpService).submitRefund(any(), any());

        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(refundId).refundStatus(RefundStatus.REFUNDCLOSE).build());

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


        // call method
        RefundRequest refundRequest = MockUtils.aRefundRequestBuilder().build();
        RefundResponse refundResponse = service.refundRequest(refundRequest);

        // assertions
        assertNotNull(refundResponse);
        assertEquals(RefundResponseFailure.class, refundResponse.getClass());
        RefundResponseFailure refundResponseFailure = (RefundResponseFailure) refundResponse;
        assertEquals(refundId, refundResponseFailure.getPartnerTransactionId());
        assertEquals("REFUNDCLOSE", refundResponseFailure.getErrorCode());
        assertEquals(FailureCause.REFUSED, refundResponseFailure.getFailureCause());
    }

    @Test
    void refundRequestOther() {
        // create Mocks
        SubmitRefundResponse submitRefundResponse = SubmitRefundResponse.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.SUCCESS)
                .refundId(refundId)
                .build();
        Mockito.doReturn(submitRefundResponse).when(httpService).submitRefund(any(), any());

        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder().refundId(refundId).refundStatus(RefundStatus.CHANGE).build());

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


        // call method
        RefundRequest refundRequest = MockUtils.aRefundRequestBuilder().build();
        RefundResponse refundResponse = service.refundRequest(refundRequest);

        // assertions
        assertNotNull(refundResponse);
        assertEquals(RefundResponseFailure.class, refundResponse.getClass());
        RefundResponseFailure refundResponseFailure = (RefundResponseFailure) refundResponse;
        assertEquals(refundId, refundResponseFailure.getPartnerTransactionId());
        assertEquals("CHANGE", refundResponseFailure.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, refundResponseFailure.getFailureCause());
    }

    @Test
    void refundRequestPluginException() {
        // create Mocks
        Mockito.doThrow(new InvalidDataException("foo")).when(httpService).submitRefund(any(), any());

        // call method
        RefundRequest refundRequest = MockUtils.aRefundRequestBuilder().build();
        RefundResponse refundResponse = service.refundRequest(refundRequest);

        // assertions
        assertNotNull(refundResponse);
        assertEquals(RefundResponseFailure.class, refundResponse.getClass());
        RefundResponseFailure refundResponseFailure = (RefundResponseFailure) refundResponse;
        assertEquals("UNKNOWN", refundResponseFailure.getPartnerTransactionId());
        assertEquals("foo", refundResponseFailure.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, refundResponseFailure.getFailureCause());
    }

    @Test
    void refundRequestRuntimeException() {
        // create Mocks
        Mockito.doThrow(new NullPointerException("foo")).when(httpService).submitRefund(any(), any());

        // call method
        RefundRequest refundRequest = MockUtils.aRefundRequestBuilder().build();
        RefundResponse refundResponse = service.refundRequest(refundRequest);

        // assertions
        assertNotNull(refundResponse);
        assertEquals(RefundResponseFailure.class, refundResponse.getClass());
        RefundResponseFailure refundResponseFailure = (RefundResponseFailure) refundResponse;
        assertEquals("UNKNOWN", refundResponseFailure.getPartnerTransactionId());
        assertEquals("plugin error: NullPointerException: foo", refundResponseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, refundResponseFailure.getFailureCause());
    }

    @Test
    void canMultiple() {
        assertTrue(service.canMultiple());
    }

    @Test
    void canPartial() {
        assertTrue(service.canPartial());
    }
}