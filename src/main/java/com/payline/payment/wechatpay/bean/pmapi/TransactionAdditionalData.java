package com.payline.payment.wechatpay.bean.pmapi;

public class TransactionAdditionalData {

    private String transactionId;

    public TransactionAdditionalData(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
