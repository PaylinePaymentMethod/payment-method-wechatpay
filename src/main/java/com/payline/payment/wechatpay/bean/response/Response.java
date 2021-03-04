package com.payline.payment.wechatpay.bean.response;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.wechatpay.bean.WeChatPayBean;
import com.payline.payment.wechatpay.bean.nested.Code;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class Response extends WeChatPayBean {

    @SerializedName("return_code")
    @NonNull
    Code returnCode;

    @SerializedName("return_msg")
    String returnMessage;

    @SerializedName("result_code")
    Code resultCode;

    // error fields
    @SerializedName("err_code")
    String errorCode;

    @SerializedName("err_code_des")
    String errorCodeDescription;

    @SerializedName("device_info")
    String deviceInfo;
}
