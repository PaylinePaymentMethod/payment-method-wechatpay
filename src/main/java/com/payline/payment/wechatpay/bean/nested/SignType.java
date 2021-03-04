package com.payline.payment.wechatpay.bean.nested;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SignType {
    MD5("MD5"), HMACSHA256("HMAC-SHA256");

    private final String type;
}
