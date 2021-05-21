package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.configuration.Acquirer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

 class AcquirerServiceTest {

    private final AcquirerService underTest = AcquirerService.getInstance();

    @Test
    void retrieveAcquirers() {
        final List<Acquirer> acquirers = underTest.retrieveAcquirers(MockUtils.PLUGIN_CONFIGURATION);

        assertNotNull(acquirers);
        assertEquals(2, acquirers.size());

        assertEquals("123456789", acquirers.get(0).getAppId());
        assertEquals("Label 1", acquirers.get(0).getLabel());
        assertEquals("merchantId1", acquirers.get(0).getMerchantId());
        assertEquals("wechatPay_key1", acquirers.get(0).getKey());
        assertEquals("certificate", acquirers.get(0).getCertificate());


        assertEquals("123456790", acquirers.get(1).getAppId());
        assertEquals("Label 2", acquirers.get(1).getLabel());
        assertEquals("merchantId2", acquirers.get(1).getMerchantId());
        assertEquals("wechatPay_key2", acquirers.get(1).getKey());
        assertEquals("certificate", acquirers.get(0).getCertificate());

    }

    @Test
    void fetchAcquirer() {
        final Acquirer acquirer = underTest.fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION,"1");

        assertNotNull(acquirer);
        assertEquals("123456789", acquirer.getAppId());
        assertEquals("Label 1", acquirer.getLabel());
        assertEquals("merchantId1", acquirer.getMerchantId());
        assertEquals("wechatPay_key1", acquirer.getKey());
        assertEquals("certificate", acquirer.getCertificate());



    }
}
