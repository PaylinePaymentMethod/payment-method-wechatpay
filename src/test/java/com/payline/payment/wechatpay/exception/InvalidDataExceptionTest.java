package com.payline.payment.wechatpay.exception;

import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvalidDataExceptionTest {

    private InvalidDataException invalidDataException;
    private String longMessage = "errorCodeOrLabelerrorCodeOrLabelerrorCodeOrLabelerrorCodeOrLabelerrorCodeOrLabel";


    @BeforeAll
    void setUp() {

        invalidDataException = new InvalidDataException("test");
        Assertions.assertEquals("test", invalidDataException.getErrorCode());
    }

    @Test
    void getFailureCause() {
        Assertions.assertEquals(FailureCause.INVALID_DATA, invalidDataException.getFailureCause());
    }


    @Test
    void toPaymentResponseFailure() {
        invalidDataException = new InvalidDataException(longMessage);

        PaymentResponseFailure paymentResponseFailure = invalidDataException.toPaymentResponseFailureBuilder().build();
        Assertions.assertEquals(FailureCause.INVALID_DATA, paymentResponseFailure.getFailureCause());
        Assertions.assertTrue(paymentResponseFailure.getErrorCode().contains("errorCodeOrLabel"));
        Assertions.assertEquals(50, paymentResponseFailure.getErrorCode().length());
    }

    @Test
    void toRefundResponseFailure() {
        invalidDataException = new InvalidDataException(longMessage);

        RefundResponseFailure refundResponseFailure = invalidDataException.toRefundResponseFailureBuilder().build();
        Assertions.assertEquals(FailureCause.INVALID_DATA, refundResponseFailure.getFailureCause());
        Assertions.assertTrue(refundResponseFailure.getErrorCode().contains("errorCodeOrLabel"));
        Assertions.assertEquals(50, refundResponseFailure.getErrorCode().length());
    }
}