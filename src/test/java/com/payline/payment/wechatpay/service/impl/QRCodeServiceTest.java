package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.QRCodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QRCodeServiceTest {
    private QRCodeService qrCodeService = QRCodeService.getInstance();

    @Test
    void generateMatrixOK() {
        BufferedImage image = qrCodeService.generateMatrix("test", 300);
        Assertions.assertNotNull(image);
    }
    @Test
    void generateMatrixKO_NullData() {
        assertThrows(PluginException.class, () -> qrCodeService.generateMatrix(null, 300));
    }
    @Test
    void generateMatrixKO_EmptyData() {
        assertThrows(PluginException.class, () -> qrCodeService.generateMatrix("test", -1));
    }
}
