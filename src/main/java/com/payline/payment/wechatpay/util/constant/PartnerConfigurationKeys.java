package com.payline.payment.wechatpay.util.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartnerConfigurationKeys {
    public static final String UNIFIED_ORDER_URL = "unifiedOrderUrl";
    public static final String QUERY_ORDER_URL = "queryOrderUrl";
    public static final String SUBMIT_REFUND_URL = "submitRefundUrl";
    public static final String QUERY_REFUND_URL = "queryRefundUrl";
    public static final String DOWNLOAD_TRANSACTIONS_URL = "downloadTransactionsUrl";

    public static final String KEY = "key";
    public static final String APPID = "appId";
    public static final String SUB_APPID = "subAppId";
    public static final String SIGN_TYPE = "signType";
    public static final String DEVICE_INFO = "device";
}
