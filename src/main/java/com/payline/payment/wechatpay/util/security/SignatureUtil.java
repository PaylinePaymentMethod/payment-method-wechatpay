package com.payline.payment.wechatpay.util.security;

import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.XMLService;
import com.payline.pmapi.bean.common.FailureCause;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class SignatureUtil {

    private static final String FIELD_SIGN = "sign";
    private static final String HMACSHA256 = "HmacSHA256";
    private static final String MD5 = "MD5";
    private static final String INVALID_PARAMETER = "Invalid parameter";
    private static final String INVALID_ALGORITHM = "Invalid algorithm";
    private static final String INVALID_KEY = "Invalid Key";

    private XMLService xmlService = XMLService.getInstance();

    // --- Singleton Holder pattern + initialization BEGIN
    private SignatureUtil(){}

    private static class Holder {
        private static final SignatureUtil instance = new SignatureUtil();
    }

    public static SignatureUtil getInstance() {
        return SignatureUtil.Holder.instance;
    }
    // --- Singleton Holder pattern + initialization END

    /**
     *  Check if data are signed correctly according to a key and a algorithm.
     * @param data Data's signature to check
     * @param key API Key
     * @param signType MAC algorithm
     * @return True if the signature is valid
     */
    public boolean isSignatureValid(Map<String, String> data, String key, String signType) {
        if(PluginUtils.isEmpty(key)){
            log.error(INVALID_PARAMETER);
            throw new PluginException(INVALID_PARAMETER, FailureCause.INVALID_DATA);
        }

        if (!data.containsKey(FIELD_SIGN)) {
            log.error("FIELD_SIGN is missing for WECHATPAY response");
            return false;
        }
        String sign = data.get(FIELD_SIGN);
        return generateSignature(data, key, signType).equals(sign);
    }

    /**
     * Generate XML format string with sign
     *
     * @param data Map type data
     * @param key  API key
     * @return XML with sign field
     */
    public String generateSignedXml(final Map<String, String> data, String key, String signType) {
        String sign = generateSignature(data, key, signType);

        data.put(FIELD_SIGN, sign);
        return xmlService.mapToXml(data);
    }

    /**
     * Generate a signature. Note that if it contains the sign_type field, it must be consistent with the signType parameter.
     *
     * @param data Data to be signed
     * @param key  API key
     * @return signature
     */
    public String generateSignature(final Map<String, String> data, String key, String signType) {

        if(PluginUtils.isEmpty(key)){
            log.error(INVALID_PARAMETER);
            throw new PluginException(INVALID_PARAMETER, FailureCause.INVALID_DATA);
        }

        StringBuilder sb = new StringBuilder(
                data.entrySet().stream()
                        .filter(e -> !e.getKey().equals(FIELD_SIGN))    // remove signature entry
                        .filter(e -> !PluginUtils.isEmpty(e.getValue()))  // remove empty entries
                        .sorted(Map.Entry.comparingByKey())             // sort entry by alphabetical keys
                        .map(e -> e.getKey() + "=" + e.getValue().trim())// create URL encoded String with remaining entries
                        .collect(Collectors.joining("&"))
        );
        sb.append("&key=").append(key);                           // add key to the end of created String

        if (signType.equals(SignType.MD5.getType())) {
            return hashWithMD5(sb.toString());
        } else {
            return hashWithSha256(sb.toString(), key);
        }
    }
    /**
     * Generate a hash with the HmacSHA256 algorithm and the provided API key
     * @param data Data to hashed
     * @param key API Key
     * @return HmacSHA256 hash of the data
     */
    public String hashWithSha256(String data, String key) {

        if(PluginUtils.isEmpty(data) || PluginUtils.isEmpty(key)){
            log.error(INVALID_PARAMETER);
            throw new PluginException(INVALID_PARAMETER, FailureCause.INVALID_DATA);
        }

        try {
            // init cipher
            Mac sha256HMAC = Mac.getInstance(HMACSHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMACSHA256);
            sha256HMAC.init(secretKeySpec);

            return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8))).toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            log.error(INVALID_ALGORITHM, e);
            throw new PluginException(INVALID_ALGORITHM, FailureCause.INVALID_DATA);
        } catch (InvalidKeyException e) {
            log.error(INVALID_KEY, e);
            throw new PluginException(INVALID_KEY, FailureCause.INVALID_DATA);
        }
    }

    /**
     * Generate a hash with the MD5 algorithm
     * @param data Data to hashed
     * @return MD5 hash of the data
     */
    public String hashWithMD5(String data) {

        if(PluginUtils.isEmpty(data)){
            log.error(INVALID_PARAMETER);
            throw new PluginException(INVALID_PARAMETER, FailureCause.INVALID_DATA);
        }

        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            md.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return Hex.encodeHexString(digest).toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            log.error(INVALID_ALGORITHM, e);
            throw new PluginException(INVALID_ALGORITHM, FailureCause.INVALID_DATA);
        }
    }
}
