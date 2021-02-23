package com.payline.payment.wechatpay.bean.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.WeChatPayBean;
import com.payline.payment.wechatpay.bean.nested.TradeType;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class UnifiedOrderRequest extends WeChatPayBean {
    String body;

    @SerializedName("out_trade_no")
    String outTradeNo;

    @SerializedName("device_info")
    String deviceInfo;

    @SerializedName("fee_type")
    String feeType;

    @SerializedName("total_fee")
    String totalFee;

    @SerializedName("spbill_create_ip")
    String spBillCreateIp;

    @SerializedName("notify_url")
    String notifyUrl;

    @SerializedName("trade_type")
    TradeType tradeType;

    @SerializedName("product_id") String productId;
}
