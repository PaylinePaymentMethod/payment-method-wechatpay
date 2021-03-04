package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.exception.InvalidDataException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;

class PaymentServiceImplTest {
    @InjectMocks
    private PaymentServiceImpl service = new PaymentServiceImpl();
    @Mock
    private HttpService httpService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequest_PluginException() {
        Mockito.doThrow(new InvalidDataException("foo")).when(httpService).unifiedOrder(any(), any());

        // when: sending the request, a PluginException is thrown
        PaymentResponse paymentResponse =  service.paymentRequest(MockUtils.aPaylinePaymentRequest());

        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        Assertions.assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) paymentResponse).getFailureCause());
    }

    @Test
    void paymentRequest_RunTimeException() {
        Mockito.doThrow(new RuntimeException("foo")).when(httpService).unifiedOrder(any(), any());

        // when: sending the request, a PluginException is thrown
        PaymentResponse paymentResponse =  service.paymentRequest(MockUtils.aPaylinePaymentRequest());

        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponse.getClass());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, ((PaymentResponseFailure) paymentResponse).getFailureCause());
    }

}
