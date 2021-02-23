package com.payline.payment.wechatpay.util;

import com.payline.pmapi.bean.common.FailureCause;

import java.util.HashMap;
import java.util.Map;

public class ErrorConverter {
    static Map<String, FailureCause> mapper = new HashMap<>();

    static {
        mapper.put("SYSTEMERROR", FailureCause.PAYMENT_PARTNER_ERROR);   //
        mapper.put("ORDERNOTEXIST", FailureCause.INVALID_DATA);
        mapper.put("NOAUTH", FailureCause.REFUSED);
        mapper.put("NOTENOUGH", FailureCause.REFUSED);
        mapper.put("ORDERPAID", FailureCause.INVALID_DATA);
        mapper.put("ORDERCLOSED", FailureCause.SESSION_EXPIRED);
        mapper.put("APPID_NOT_EXIST", FailureCause.INVALID_DATA);
        mapper.put("MCHID_NOT_EXIST", FailureCause.INVALID_DATA);
        mapper.put("APPID_MCHID_NOT_MATCH", FailureCause.INVALID_DATA);
        mapper.put("LACK_PARAMS", FailureCause.INVALID_DATA);
        mapper.put("OUT_TRADE_NO_USED", FailureCause.INVALID_DATA);
        mapper.put("SIGNERROR", FailureCause.INVALID_DATA);
        mapper.put("XML_FORMAT_ERROR", FailureCause.INVALID_FIELD_FORMAT);
        mapper.put("POST_DATA_EMPTY", FailureCause.INVALID_DATA);
        mapper.put("NOT_UTF8", FailureCause.INVALID_DATA);
        mapper.put("USER_ACCOUNT_ABNORMAL", FailureCause.REFUSED);
        mapper.put("INVALID_TRANSACTIONID", FailureCause.INVALID_DATA);
        mapper.put("REQUIRE_POST_METHOD", FailureCause.INVALID_DATA);
        mapper.put("REFUNDNOTEXIST", FailureCause.INVALID_DATA);
        mapper.put("PARAM_ERROR", FailureCause.INVALID_DATA);

        // Download transaction history error code
        mapper.put("20003", FailureCause.PAYMENT_PARTNER_ERROR);
        mapper.put("20001", FailureCause.INVALID_DATA);
        mapper.put("20002", FailureCause.INVALID_DATA);
        mapper.put("20007", FailureCause.REFUSED);
        mapper.put("200100", FailureCause.PAYMENT_PARTNER_ERROR);
    }

    private ErrorConverter() {
    }

    private static class Holder {
        private static final ErrorConverter instance = new ErrorConverter();
    }

    public static ErrorConverter getInstance() {
        return ErrorConverter.Holder.instance;
    }
    // --- Singleton Holder pattern + initialization END



    public FailureCause convert(String message) {
        return mapper.getOrDefault(message.toUpperCase(), FailureCause.PARTNER_UNKNOWN_ERROR);
    }
}
