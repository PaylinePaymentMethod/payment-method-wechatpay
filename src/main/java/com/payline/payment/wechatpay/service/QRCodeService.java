package com.payline.payment.wechatpay.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.pmapi.bean.common.FailureCause;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class QRCodeService {
    private static final String QRCODE_GENERATION_ERROR = "QRCode generation error";
    public static final String IMAGE_FORMAT = "png";

    private static class Holder {
        private static final QRCodeService INSTANCE = new QRCodeService();
    }

    public static QRCodeService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Return an image as a byte array representing a QRCode representation of data
     * @param data String containing the data that will be converted in QRCode
     * @param size Size of the buffered image
     * @return QRCode Buffered image
     * @throws PluginException
     */
    public byte[] generateImage(final String data, final int size) {
        if (PluginUtils.isEmpty(data)) {
            log.error(QRCODE_GENERATION_ERROR + " : Empty data");
            throw new PluginException(QRCODE_GENERATION_ERROR, FailureCause.INVALID_DATA);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size);
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (final IllegalArgumentException | WriterException | IOException e) {
            log.error(QRCODE_GENERATION_ERROR, e);
            throw new PluginException(QRCODE_GENERATION_ERROR, FailureCause.INVALID_DATA);
        }
    }
}
