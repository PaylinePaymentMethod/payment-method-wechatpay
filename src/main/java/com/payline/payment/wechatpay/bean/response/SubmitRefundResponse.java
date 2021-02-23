package com.payline.payment.wechatpay.bean.response;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
@ToString
public class SubmitRefundResponse extends Response{

    @SerializedName("transaction_id")
    String transactionId;

    @SerializedName("out_trade_no")
    String outTradeNo;

    @SerializedName("out_refund_no")
    String outRefundNo;

    @SerializedName("refund_id")
    String refundId;

    @SerializedName("refund_fee")
    String refundFee;

    @SerializedName("refund_fee_type")
    String refundFeeType;

    @SerializedName("total_fee")
    String totalFee;

    @SerializedName("fee_type")
    String feeType;

    @SerializedName("cash_fee")
    String cashFee;

    @SerializedName("cash_fee_type")
    String cashFeeType;

    @SerializedName("cash_refund_fee")
    String cashRefundFee;

    @SerializedName("cash_refund_fee_type")
    String cashRefundFeeType;

    String rate;
}
