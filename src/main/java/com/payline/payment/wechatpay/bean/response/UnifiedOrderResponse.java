package com.payline.payment.wechatpay.bean.response;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.nested.TradeType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
@ToString
public class UnifiedOrderResponse extends Response{

    @SerializedName("prepay_id")
    String prepayId;

    @SerializedName("trade_type")
    TradeType tradeType;

    @SerializedName("code_url")
    String codeUrl;
}
