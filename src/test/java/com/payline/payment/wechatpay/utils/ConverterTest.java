package com.payline.payment.wechatpay.utils;

import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.nested.Refund;
import com.payline.payment.wechatpay.bean.nested.RefundStatus;
import com.payline.payment.wechatpay.bean.response.QueryRefundResponse;
import com.payline.payment.wechatpay.util.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConverterTest {
    private Converter converter = Converter.getInstance();

    @BeforeEach
    void setUp() {
    }

    @Test
    void xmlConverter() {
        String s ="<xml>" +
                "<appid><![CDATA[wx2421b1c4370ec43b]]></appid>" +
                "<mch_id><![CDATA[10000100]]></mch_id>" +
                "<sub_mch_id><![CDATA[123321]]></sub_mch_id>" +
                "<nonce_str><![CDATA[TeqClE3i0mvn3DrK]]></nonce_str>" +
                "<out_refund_no_0><![CDATA[1415701182]]></out_refund_no_0>" +
                "<out_trade_no><![CDATA[1415757673]]></out_trade_no>" +
                "<refund_count>1</refund_count>" +
                "<refund_fee_0>1</refund_fee_0>" +
                "<refund_id_0><![CDATA[2008450740201411110000174436]]></refund_id_0>" +
                "<refund_status_0><![CDATA[PROCESSING]]></refund_status_0>" +
                "<result_code><![CDATA[SUCCESS]]></result_code>" +
                "<return_code><![CDATA[SUCCESS]]></return_code>" +
                "<return_msg><![CDATA[OK]]></return_msg>" +
                "<sign><![CDATA[1F2841558E233C33ABA71A961D27561C]]></sign>" +
                "<transaction_id><![CDATA[1008450740201411110005820873]]></transaction_id>" +
                "</xml>";

        List<Refund> refunds = new ArrayList<>();
        refunds.add(Refund.builder()
                .outRefundNo("1415701182")
                .refundId("2008450740201411110000174436")
                .refundFee("1")
                .refundStatus(RefundStatus.PROCESSING)
                .build());

        QueryRefundResponse expectedResponse = QueryRefundResponse.builder()
                .appId("wx2421b1c4370ec43b")
                .merchantId("10000100")
                .subMerchantId("123321")
                .nonceStr("TeqClE3i0mvn3DrK")
                .outTradeNo("1415757673")
                .refundCount("1")
                .resultCode(Code.SUCCESS)
                .returnCode(Code.SUCCESS)
                .returnMessage("OK")
                .sign("1F2841558E233C33ABA71A961D27561C")
                .transactionId("1008450740201411110005820873")
                .refunds(refunds)
                .build();


        QueryRefundResponse response = converter.createQueryResponse(s);

        assertEquals(expectedResponse, response);
    }
}