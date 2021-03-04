package com.payline.payment.wechatpay.bean.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.WeChatPayBean;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class QueryRefundRequest extends WeChatPayBean {
    @SerializedName("refund_id")
    String refundId;

    @SerializedName("transaction_id")
    String transactionId;

    @SerializedName("out_trade_no")
    String outTradeNo;
}
