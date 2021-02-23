package com.payline.payment.wechatpay.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.pmapi.bean.common.FailureCause;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;

@Log4j2
public class QRCodeService {
    private static final String QRCODE_GENERATION_ERROR = "QRCode generation error";

    // --- Singleton Holder pattern + initialization BEGIN
    private QRCodeService() {
    }

    private static class Holder {
        private static final QRCodeService instance = new QRCodeService();
    }

    public static QRCodeService getInstance() {
        return Holder.instance;
    }
    // --- Singleton Holder pattern + initialization END

    /**
     * Return an BufferedImage representing a QRCode representation of data
     * @param data String containing the data that will be converted in QRCode
     * @param size Size of the buffered image
     * @return QRCode Buffered image
     * @throws PluginException
     */
    public BufferedImage generateMatrix(String data, int size) {
        if(PluginUtils.isEmpty(data)){
            log.error(QRCODE_GENERATION_ERROR + " : Empty data");
            throw new PluginException(QRCODE_GENERATION_ERROR, FailureCause.INVALID_DATA);
        }
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        }catch (IllegalArgumentException | WriterException e){
            log.error(QRCODE_GENERATION_ERROR, e);
            throw new PluginException(QRCODE_GENERATION_ERROR, FailureCause.INVALID_DATA);
        }
    }
}
