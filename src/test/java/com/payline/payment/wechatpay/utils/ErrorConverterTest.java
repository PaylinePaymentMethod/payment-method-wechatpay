package com.payline.payment.wechatpay.utils;

import com.payline.payment.wechatpay.util.ErrorConverter;
import com.payline.pmapi.bean.common.FailureCause;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ErrorConverterTest {
    ErrorConverter converter = ErrorConverter.getInstance();

    private static Stream<Arguments> errorCodes() {
        return Stream.of(
                Arguments.of("SYSTEMERROR", FailureCause.PAYMENT_PARTNER_ERROR),
                Arguments.of("ORDERNOTEXIST", FailureCause.INVALID_DATA),
                Arguments.of("NOAUTH", FailureCause.REFUSED),
                Arguments.of("NOTENOUGH", FailureCause.REFUSED),
                Arguments.of("ORDERPAID", FailureCause.INVALID_DATA),
                Arguments.of("ORDERCLOSED", FailureCause.SESSION_EXPIRED),
                Arguments.of("APPID_NOT_EXIST", FailureCause.INVALID_DATA),
                Arguments.of("MCHID_NOT_EXIST", FailureCause.INVALID_DATA),
                Arguments.of("APPID_MCHID_NOT_MATCH", FailureCause.INVALID_DATA),
                Arguments.of("LACK_PARAMS", FailureCause.INVALID_DATA),
                Arguments.of("OUT_TRADE_NO_USED", FailureCause.INVALID_DATA),
                Arguments.of("SIGNERROR", FailureCause.INVALID_DATA),
                Arguments.of("XML_FORMAT_ERROR", FailureCause.INVALID_FIELD_FORMAT),
                Arguments.of("POST_DATA_EMPTY", FailureCause.INVALID_DATA),
                Arguments.of("NOT_UTF8", FailureCause.INVALID_DATA),
                Arguments.of("USER_ACCOUNT_ABNORMAL", FailureCause.REFUSED),
                Arguments.of("INVALID_TRANSACTIONID", FailureCause.INVALID_DATA),
                Arguments.of("REQUIRE_POST_METHOD", FailureCause.INVALID_DATA),
                Arguments.of("REFUNDNOTEXIST", FailureCause.INVALID_DATA),
                Arguments.of("PARAM_ERROR", FailureCause.INVALID_DATA),

                // Download transaction history error code
                Arguments.of("20003", FailureCause.PAYMENT_PARTNER_ERROR),
                Arguments.of("20001", FailureCause.INVALID_DATA),
                Arguments.of("20002", FailureCause.INVALID_DATA),
                Arguments.of("20007", FailureCause.REFUSED),
                Arguments.of("200100", FailureCause.PAYMENT_PARTNER_ERROR)
        );

    }


    @ParameterizedTest
    @MethodSource("errorCodes")
    void getFailureCause(String errorCode, FailureCause cause) {
        Assertions.assertEquals(cause, converter.convert(errorCode));
    }
}