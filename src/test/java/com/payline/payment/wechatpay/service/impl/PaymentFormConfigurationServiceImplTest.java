package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentFormConfigurationServiceImplTest {
    private PaymentFormConfigurationServiceImpl service = new PaymentFormConfigurationServiceImpl();


    @Test
    void getPaymentFormConfiguration() {

        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(MockUtils.aPaymentFormConfigurationRequest());

        Assertions.assertEquals(PaymentFormConfigurationResponseSpecific.class, response.getClass());
        PaymentFormConfigurationResponseSpecific responseSpecific = (PaymentFormConfigurationResponseSpecific) response;
        Assertions.assertTrue(responseSpecific.getPaymentForm().isDisplayButton());
        Assertions.assertEquals("???fr_FR.form.button.text???", responseSpecific.getPaymentForm().getButtonText());
        Assertions.assertEquals("???fr_FR.form.button.description???", responseSpecific.getPaymentForm().getDescription());
    }
}
