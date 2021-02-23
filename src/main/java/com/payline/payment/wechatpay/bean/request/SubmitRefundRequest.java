package com.payline.payment.wechatpay.bean.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.WeChatPayBean;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class SubmitRefundRequest extends WeChatPayBean {

    @SerializedName("transaction_id")
    String transactionId;

    @SerializedName("out_trade_no")
    String outTradeNo;

    @SerializedName("out_refund_no")
    String outRefundNo;

    @SerializedName("total_fee")
    String totalFee;

    @SerializedName("refund_fee")
    String refundFee;

    @SerializedName("refund_fee_type")
    String refundFeeType;
}
