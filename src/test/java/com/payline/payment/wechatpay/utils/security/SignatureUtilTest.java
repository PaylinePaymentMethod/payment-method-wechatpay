package com.payline.payment.wechatpay.utils.security;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.Converter;
import com.payline.payment.wechatpay.util.security.SignatureUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void generateSignedXml_HMACSHA256() throws URISyntaxException, IOException {
        Path resourcePath = Paths.get(this.getClass().getResource("/generateSignedXml_HMACSHA256.xml").toURI());
        String expectedXml = new String(Files.readAllBytes(resourcePath), "UTF8");
        Map<String, String> data = converter.objectToMap(MockUtils.aHMACSHA256Response());
        String key = "key";
        SignType signType = SignType.HMACSHA256;

        String signedXml = utils.generateSignedXml(data, key, signType.getType());
        Assertions.assertEquals(expectedXml, signedXml);
    }
    @Test
    void generateSignedXml_MD5() throws URISyntaxException, IOException {
        Path resourcePath = Paths.get(this.getClass().getResource("/generateSignedXml_MD5.xml").toURI());
        String expectedXml = new String(Files.readAllBytes(resourcePath), "UTF8");
        Map<String, String> data = converter.objectToMap(MockUtils.aMD5Response());
        String key = "key";
        SignType signType = SignType.MD5;

        String signedXml = utils.generateSignedXml(data, key, signType.getType());

        Assertions.assertEquals(expectedXml, signedXml);
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

    @Test
    void hashWithMD5(){
        String rawString = "appid=wxa5b511bc130a4d9e&device_info=WEB&err_code=INVALID_REQUEST&err_code_des=201 商户订单号重复&" +
                "mch_id=110605603&nonce_str=zg9vizP9FSWKP9ku&result_code=FAIL&return_code=SUCCESS&return_msg=OK&" +
                "sub_mch_id=345923236&key=2ab9071b06b9f739b950ddb41db2690d";
        assertEquals("E06AFA945146A138D7F7C3CF4C13234F", utils.hashWithMD5(rawString));
    }
}