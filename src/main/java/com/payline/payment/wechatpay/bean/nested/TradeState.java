package com.payline.payment.wechatpay.bean.nested;

public enum TradeState {
    SUCCESS     // Payment successful
    , REFUND    // Order to be refunded
    , NOTPAY    // Order not paid
    , CLOSED    // Order closed
    , REVOKED   // Order revoked
    , USERPAYING// Awaiting user to pay
    , PAYERROR  // Payment failed
}
