package com.payline.payment.wechatpay.utils.security;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.Converter;
import com.payline.payment.wechatpay.util.security.SignatureUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SignatureUtilTest {

    private SignatureUtil utils = SignatureUtil.getInstance();
    private Converter converter = Converter.getInstance();

    @Test
    void isSignatureValid_KO(){
        Map<String, String> respData = converter.objectToMap(MockUtils.aResponseWithoutSign());
        String key = "key";

        Assertions.assertFalse(utils.isSignatureValid(respData, key, SignType.HMACSHA256.getType()));
    }
    @Test
    void isSignatureValid_NullKey(){
        Map<String, String> respData = converter.objectToMap(MockUtils.aResponseWithoutSign());
        String key = null;
        String signType = SignType.HMACSHA256.getType();
        assertThrows(PluginException.class, () -> utils.isSignatureValid(respData, key,signType));
    }
    @Test
    void isSignatureValid_OK_HMACSHA256(){
        Map<String, String> respData = converter.objectToMap(MockUtils.aHMACSHA256Response());

        Assertions.assertTrue(utils.isSignatureValid(respData, "key", SignType.HMACSHA256.getType()));
    }
    @Test
    void isSignatureValid_OK_MD5(){
        Map<String, String> respData = converter.objectToMap(MockUtils.aMD5Response());

        Assertions.assertTrue(utils.isSignatureValid(respData, "key", SignType.MD5.getType()));
    }
    @Test
    void generateSignedXml_HMACSHA256(){
        Map<String, String> data = converter.objectToMap(MockUtils.aHMACSHA256Response());
        String key = "key";
        SignType signType = SignType.HMACSHA256;

        String signedXml = utils.generateSignedXml(data, key, signType.getType());
        Assertions.assertEquals(MockUtils.aHMACSHA256SignedXml(), signedXml);
    }
    @Test
    void generateSignedXml_MD5(){
        Map<String, String> data = converter.objectToMap(MockUtils.aMD5Response());
        String key = "key";
        SignType signType = SignType.MD5;

        String signedXml = utils.generateSignedXml(data, key, signType.getType());

        Assertions.assertEquals(MockUtils.aMD5SignedXml(), signedXml);
    }
    @Test
    void generateSignedXml_NullKey(){
        Map<String, String> data = converter.objectToMap(MockUtils.aHMACSHA256Response());
        String key = null;
        String signType = SignType.HMACSHA256.getType();

        assertThrows(PluginException.class, () -> utils.generateSignedXml(data, key, signType));
    }
    @Test
    void hashWithSha256_KeyKO() {
        Map<String, String> data = converter.objectToMap(MockUtils.aHMACSHA256Response());
        String key = "";

        StringBuilder sb = new StringBuilder(
                data.entrySet().stream()
                        .filter(e -> !e.getKey().equals("sign"))    // remove signature entry
                        .filter(e -> e.getValue().trim().length() > 0)  // remove empty entries
                        .sorted(Map.Entry.comparingByKey())             // sort entry by alphabetical keys
                        .map(e -> e.getKey() + "=" + e.getValue().trim())// create URL encoded String with remaining entries
                        .collect(Collectors.joining("&"))
        );

        sb.append("&key=").append(key);

        String stringToHash = sb.toString();
        assertThrows(PluginException.class, () -> utils.hashWithSha256(stringToHash, key));
    }

    @Test
    void hashWithMD5_Null(){
        assertThrows(PluginException.class, () -> utils.hashWithMD5(null));
    }
}