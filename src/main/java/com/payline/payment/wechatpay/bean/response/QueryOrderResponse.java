package com.payline.payment.wechatpay.bean.response;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.nested.TradeState;
import com.payline.payment.wechatpay.bean.nested.TradeType;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
@ToString
public class QueryOrderResponse extends Response {

    @SerializedName("openid")
    String openId;

    @SerializedName("is_subscribe")
    String isSubscribe;

    @SerializedName("sub_openid")
    String subOpenId;

    @SerializedName("sub_is_subscribe")
    String subIsSubscribe;

    @SerializedName("trade_type")
    TradeType tradeType;


    @SerializedName("trade_state")
    @NonNull
    TradeState tradeState;

    @SerializedName("bank_type")
    String bankType;

    @SerializedName("total_fee")
    String totalFee;

    @SerializedName("fee_type")
    String feeType;

    @SerializedName("cash_fee")
    String cashFee;

    @SerializedName("cash_fee_type")
    String cashFeeType;

    @SerializedName("transaction_id")
    String transactionId;

    @SerializedName("out_trade_no")
    String outTradNo;

    String attach;

    @SerializedName("time_end")
    String timeEnd;

    @SerializedName("trade_state_desc")
    String tradeStateDesc;

    String rate;
}
