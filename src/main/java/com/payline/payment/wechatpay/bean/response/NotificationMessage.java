package com.payline.payment.wechatpay.bean.response;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class NotificationMessage extends Response{

    @SerializedName("transaction_id")
    String transactionId;


}
