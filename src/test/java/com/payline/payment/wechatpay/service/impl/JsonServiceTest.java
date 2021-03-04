package com.payline.payment.wechatpay.service.impl;


import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.request.DownloadTransactionHistoryRequest;
import com.payline.payment.wechatpay.util.JsonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonServiceTest {

    JsonService jsonService = JsonService.getInstance();

    String expectedJson = "{\"bill_date\":\"2020/12/15\"," +
            "\"bill_type\":\"test\"," +
            "\"appid\":\"00000\"," +
            "\"mch_id\":\"merchantIdTest\"," +
            "\"sub_appid\":\"subAppIdTest\"," +
            "\"sub_mch_id\":\"subMerchantIdTest\"," +
            "\"nonce_str\":\"aaaaaaa\"," +
            "\"sign_type\":\"" + SignType.HMACSHA256.getType() + "\"," +
            "\"sign\":\"azerty\"" +
            "}";

    DownloadTransactionHistoryRequest expectedDownloadTransactionHistoryRequest =  DownloadTransactionHistoryRequest.builder()
            .billDate("2020/12/15")
            .billType("test")
            .appId("00000")
            .merchantId("merchantIdTest")
            .nonceStr("aaaaaaa")
            .signType(SignType.HMACSHA256.getType())
            .sign("azerty")
            .subAppId("subAppIdTest")
            .subMerchantId("subMerchantIdTest")
            .build();

    @Test
    void fromJson() {
        DownloadTransactionHistoryRequest downloadTransactionHistoryRequest = jsonService.fromJson(expectedJson, DownloadTransactionHistoryRequest.class);
        Assertions.assertEquals(expectedDownloadTransactionHistoryRequest.getBillDate(), downloadTransactionHistoryRequest.getBillDate());
        Assertions.assertEquals(expectedDownloadTransactionHistoryRequest.getBillType(), downloadTransactionHistoryRequest.getBillType());
    }


    @Test
    void toJson() {
        Assertions.assertEquals(expectedJson, jsonService.toJson(expectedDownloadTransactionHistoryRequest));
    }
}
