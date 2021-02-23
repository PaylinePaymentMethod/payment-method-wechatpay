package com.payline.payment.wechatpay.bean.response;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.nested.Refund;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class QueryRefundResponse extends Response{

    @SerializedName("transaction_id")
    String transactionId;

    @SerializedName("out_trade_no")
    String outTradeNo;

    @SerializedName("total_fee")
    String totalFee;

    @SerializedName("fee_type")
    String feeType;

    @SerializedName("cash_fee")
    String cashFee;

    @SerializedName("cash_fee_type")
    String cashFeeType;

    @SerializedName("refund_count")
    String refundCount;

    @SerializedName("refunds")
    List<Refund> refunds;

    @SerializedName("refund_channel")
    String refundChannel;
}
