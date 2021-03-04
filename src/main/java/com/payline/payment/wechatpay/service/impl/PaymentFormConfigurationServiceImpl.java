package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.service.LogoPaymentFormConfigurationService;
import com.payline.payment.wechatpay.util.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.bean.form.NoFieldForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;

public class PaymentFormConfigurationServiceImpl extends LogoPaymentFormConfigurationService {
    private static final I18nService i18nService = I18nService.getInstance();

    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        NoFieldForm noFieldForm = NoFieldForm.NoFieldFormBuilder.aNoFieldForm()
                .withDisplayButton(true)
                .withButtonText(i18nService.getMessage("form.button.text", paymentFormConfigurationRequest.getLocale()))
                .withDescription(i18nService.getMessage("form.button.description", paymentFormConfigurationRequest.getLocale()))
                .build();

        return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder.aPaymentFormConfigurationResponseSpecific()
                .withPaymentForm(noFieldForm)
                .build();
    }
}
