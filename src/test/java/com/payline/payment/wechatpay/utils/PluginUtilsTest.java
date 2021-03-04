package com.payline.payment.wechatpay.utils;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.pmapi.bean.common.Amount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginUtilsTest {

    @Test
    void truncate() {
        Assertions.assertEquals("", PluginUtils.truncate("foo", 0));
        Assertions.assertEquals("foo", PluginUtils.truncate("foo", 3));
        Assertions.assertEquals("foo", PluginUtils.truncate("foo", 5));
    }

    @Test
    void isEmpty() {
        Assertions.assertTrue(PluginUtils.isEmpty(null));
        Assertions.assertTrue(PluginUtils.isEmpty(""));
        Assertions.assertTrue(PluginUtils.isEmpty("   "));
        Assertions.assertFalse(PluginUtils.isEmpty("foo"));
    }

    @Test
    void createDate() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateInString = "12/12/2012";

        Date date = formatter.parse(dateInString);
        Assertions.assertEquals(8, PluginUtils.createDate().length());
    }
    @Test
    void createStringAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals("0.00", PluginUtils.createStringAmount( new Amount( int1, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.01", PluginUtils.createStringAmount(new Amount(int2, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.10", PluginUtils.createStringAmount(new Amount(int3, Currency.getInstance("EUR"))));
        Assertions.assertEquals("1.00", PluginUtils.createStringAmount(new Amount(int4, Currency.getInstance("EUR"))));
        Assertions.assertEquals("10.00", PluginUtils.createStringAmount(new Amount(int5, Currency.getInstance("EUR"))));
    }
    @Test
    void createStringAmountToShow() {
        String amountToShow = PluginUtils.createStringAmountToShow(MockUtils.aPaylineAmount());
        Assertions.assertEquals("0.10€", amountToShow);
    }


    @Test
    void runtimeErrorCode(){
        // A NullPointerException is thrown
        String errorCode = null;
        String str = null;
        try {
            str.equals("toto");
        }
        catch( RuntimeException e ){
            errorCode = PluginUtils.runtimeErrorCode( e );
        }

        // the message is null, so the error code contains the exception class name
        assertEquals("plugin error: NullPointerException", errorCode);
    }

    @Test
    void addIfExist() {
        String s = "test";
        assertEquals(" " + s, PluginUtils.addIfExist(s));
    }

    @Test
    void addIfExist_Empty() {
        String s = "";
        assertEquals("", PluginUtils.addIfExist(s));
    }

    @Test
    void generateRandomString(){
        Assertions.assertEquals(10, PluginUtils.generateRandomString(10).length());
    }
}
