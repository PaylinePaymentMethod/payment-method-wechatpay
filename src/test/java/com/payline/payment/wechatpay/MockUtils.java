package com.payline.payment.wechatpay;

import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.response.Response;
import com.payline.payment.wechatpay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.util.*;

@UtilityClass
public class MockUtils {
    public final String TRANSACTION_ID = "123456789012345678901";
    public final String PARTNER_TRANSACTION_ID = "098765432109876543210";

    /**
     * Generate a valid Payline Amount.
     */
    public com.payline.pmapi.bean.common.Amount aPaylineAmount() {
        return new com.payline.pmapi.bean.common.Amount(BigInteger.valueOf(10), Currency.getInstance("EUR"));
    }

    /**
     * Generate a valid, but not complete, {@link Order}
     */
    public Order aPaylineOrder() {
        List<Order.OrderItem> items = new ArrayList<>();

        items.add(Order.OrderItem.OrderItemBuilder
                .anOrderItem()
                .withReference("foo")
                .withAmount(aPaylineAmount())
                .withQuantity((long) 1)
                .build());

        return Order.OrderBuilder.anOrder()
                .withDate(new Date())
                .withAmount(aPaylineAmount())
                .withItems(items)
                .withReference("ORDER-REF-123456")
                .build();
    }

    /**
     * Generate a valid {@link Buyer}.
     */
    public Buyer aBuyer() {
        return Buyer.BuyerBuilder.aBuyer()
                .withFullName(new Buyer.FullName("Marie", "Durand", "1"))
                .withEmail("foo@bar.baz")
                .build();
    }


    /**
     * Generate a valid {@link ContractConfiguration} to verify the connection to the API.
     */
    public ContractConfiguration aContractConfiguration() {

        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put(ContractConfigurationKeys.MERCHANT_ID, new ContractProperty("MERCHANTID"));
        contractProperties.put(ContractConfigurationKeys.SUB_MERCHANT_ID, new ContractProperty("SUBMERCHANTID"));
        contractProperties.put(ContractConfigurationKeys.SECONDARY_MERCHANT_ID, new ContractProperty("1314520"));
        contractProperties.put(ContractConfigurationKeys.MERCHANT_BANK_CODE, new ContractProperty("12345"));
        contractProperties.put(ContractConfigurationKeys.PARTNER_TRANSACTION_ID, new ContractProperty(PartnerTransactionIdOptions.ORDER_REFERENCE.name()));
        contractProperties.put(ContractConfigurationKeys.TERMINAL_NUMBER, new ContractProperty("003"));
        contractProperties.put(ContractConfigurationKeys.NUM_CONTRACT_WECHAT, new ContractProperty("999003"));


        return new ContractConfiguration("WeChatPay", contractProperties);
    }

    /**
     * Generate a valid {@link PartnerConfiguration}.
     */
    public PartnerConfiguration aPartnerConfiguration() {
        Map<String, String> partnerConfigurationMap = new HashMap<>();

        partnerConfigurationMap.put(PartnerConfigurationKeys.APPID, "123456789");
        partnerConfigurationMap.put(PartnerConfigurationKeys.DEVICE_INFO, "WEB");
        partnerConfigurationMap.put(PartnerConfigurationKeys.QUERY_ORDER_URL, "https://api.mch.weixin.qq.com/pay/orderquery");
        partnerConfigurationMap.put(PartnerConfigurationKeys.SUBMIT_REFUND_URL, "https://api.mch.weixin.qq.com/secapi/pay/refund");
        partnerConfigurationMap.put(PartnerConfigurationKeys.UNIFIED_ORDER_URL, "https://api.mch.weixin.qq.com/pay/unifiedorder");
        partnerConfigurationMap.put(PartnerConfigurationKeys.QUERY_REFUND_URL, "https://api.mch.weixin.qq.com/pay/queryrefund");
        partnerConfigurationMap.put(PartnerConfigurationKeys.DOWNLOAD_TRANSACTIONS_URL, "https://api.mch.weixin.qq.com/pay/downloadbill");
        partnerConfigurationMap.put(PartnerConfigurationKeys.KEY, "key");
        partnerConfigurationMap.put(PartnerConfigurationKeys.SUB_APPID, "");
        partnerConfigurationMap.put(PartnerConfigurationKeys.SIGN_TYPE, "MD5");

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();

        return new PartnerConfiguration(partnerConfigurationMap, sensitiveConfigurationMap);
    }

    /**
     * Generate a valid {@link Environment}.
     */
    public Environment anEnvironment() {
        return new Environment("http://notificationURL.com",
                "http://redirectionURL.com",
                "http://redirectionCancelURL.com",
                true);
    }

    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance.
     */
    public Map<String, String> anAccountInfo() {
        return anAccountInfo(aContractConfiguration());
    }

    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance,
     * from the given {@link ContractConfiguration}.
     *
     * @param contractConfiguration The model object from which the properties will be copied
     */
    public Map<String, String> anAccountInfo(ContractConfiguration contractConfiguration) {
        Map<String, String> accountInfo = new HashMap<>();
        for (Map.Entry<String, ContractProperty> entry : contractConfiguration.getContractProperties().entrySet()) {
            accountInfo.put(entry.getKey(), entry.getValue().getValue());
        }
        return accountInfo;
    }


    // Request creation methods

    /**
     * Generate a valid {@link PaymentFormLogoRequest}.
     */
    public PaymentFormLogoRequest aPaymentFormLogoRequest() {
        return PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withLocale(Locale.getDefault())
                .build();
    }


    /**
     * Generate a builder for a valid {@link ContractParametersCheckRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public ContractParametersCheckRequest.CheckRequestBuilder aContractParametersCheckRequestBuilder() {
        return ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                .withAccountInfo(anAccountInfo())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withLocale(Locale.getDefault())
                .withPartnerConfiguration(aPartnerConfiguration());
    }
    public Response aResponseWithoutSign(){
        return Response.builder()
                .returnCode(Code.SUCCESS)
                .errorCode("errorCode")
                .resultCode(Code.SUCCESS)
                .errorCodeDescription("errorCodeDescription")
                .returnMessage("returnMessage")
                .appId("appId")
                .merchantId("merchantId")
                .nonceStr("nonceStr")
                .signType(SignType.HMACSHA256.getType())
                .subAppId("subAppId")
                .subMerchantId("subMerchantId")
                .build();
    }
    public Response aHMACSHA256Response(){
        return Response.builder()
                .returnCode(Code.SUCCESS)
                .errorCode("errorCode")
                .resultCode(Code.SUCCESS)
                .errorCodeDescription("errorCodeDescription")
                .returnMessage("returnMessage")
                .appId("appId")
                .merchantId("merchantId")
                .nonceStr("nonceStr")
                .sign("413437B51D1A196D92B015946FF20B3D77D48A81A55559CAE9AE029B54F61CAB")
                .signType(SignType.HMACSHA256.getType())
                .subAppId("subAppId")
                .subMerchantId("subMerchantId")
                .build();
    }
    public Response aMD5Response(){
        return Response.builder()
                .returnCode(Code.SUCCESS)
                .errorCode("errorCode")
                .resultCode(Code.SUCCESS)
                .errorCodeDescription("errorCodeDescription")
                .returnMessage("returnMessage")
                .appId("appId")
                .merchantId("merchantId")
                .nonceStr("nonceStr")
                .sign("C4C4063BA822FC13EA0778CC700849A8")
                .signType(SignType.MD5.getType())
                .subAppId("subAppId")
                .subMerchantId("subMerchantId")
                .build();
    }

    public String aQueryRefundResponseXml(){
        return "<xml>\n" +
                "<appid><![CDATA[wxa5b511bc130a4d9e]]></appid>\n" +
                "<cash_fee><![CDATA[7]]></cash_fee>\n" +
                "<cash_fee_type><![CDATA[CNY]]></cash_fee_type>\n" +
                "<fee_type><![CDATA[EUR]]></fee_type>\n" +
                "<mch_id><![CDATA[110605603]]></mch_id>\n" +
                "<nonce_str><![CDATA[KFObVRM5NQqrrnLn]]></nonce_str>\n" +
                "<out_refund_no_0><![CDATA[00002]]></out_refund_no_0>\n" +
                "<out_trade_no><![CDATA[PAYLINE00000002]]></out_trade_no>\n" +
                "<rate><![CDATA[776703200]]></rate>\n" +
                "<refund_account_0><![CDATA[REFUND_SOURCE_UNSETTLED_FUNDS]]></refund_account_0>\n" +
                "<refund_channel_0><![CDATA[ORIGINAL]]></refund_channel_0>\n" +
                "<refund_count>1</refund_count>\n" +
                "<refund_fee>1</refund_fee>\n" +
                "<refund_fee_0>1</refund_fee_0>\n" +
                "<refund_id_0><![CDATA[50201107072021020506055269794]]></refund_id_0>\n" +
                "<refund_recv_accout_0><![CDATA[支付用户的零钱]]></refund_recv_accout_0>\n" +
                "<refund_status_0><![CDATA[SUCCESS]]></refund_status_0>\n" +
                "<refund_success_time_0><![CDATA[2021-02-05 21:13:02]]></refund_success_time_0>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "<sign><![CDATA[DA9BEA8B2F523C8D2EB344E243D2CC56]]></sign>\n" +
                "<sub_mch_id><![CDATA[345923236]]></sub_mch_id>\n" +
                "<total_fee><![CDATA[1]]></total_fee>\n" +
                "<transaction_id><![CDATA[4200000941202102052952064679]]></transaction_id>\n" +
                "</xml>";
    }


    /**
     * Generate a builder for a valid {@link RefundRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public RefundRequest.RefundRequestBuilder aRefundRequestBuilder() {
        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(aPaylineAmount())
                .withOrder(aPaylineOrder())
                .withBuyer(aBuyer())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withTransactionId(TRANSACTION_ID)
                .withPartnerTransactionId(PARTNER_TRANSACTION_ID)
                .withPartnerConfiguration(aPartnerConfiguration());
    }


    public NotificationRequest.NotificationRequestBuilder aNotificationRequestBuilder(){
        return NotificationRequest.NotificationRequestBuilder
                .aNotificationRequest()
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withHttpMethod("POST")
                .withHeaderInfos(new HashMap<>())
                .withPathInfo("aPathInfo")
                ;
    }
    /**
     * Generate a valid {@link PaymentRequest}.
     */
    public PaymentRequest aPaylinePaymentRequest() {
        return aPaylinePaymentRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link PaymentRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public PaymentRequest.Builder aPaylinePaymentRequestBuilder() {
        long randomTransactionId = (long) (Math.random() * 100000000000000L);
        return PaymentRequest.builder()
                .withAmount(aPaylineAmount())
                .withBrowser(aBrowser())
                .withBuyer(aBuyer())
                .withCaptureNow(true)
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withLocale(Locale.getDefault())
                .withOrder(aPaylineOrder())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withPaymentFormContext(aPaymentFormContext())
                .withSoftDescriptor("Test")
                .withTransactionId("PAYLINE"+randomTransactionId);
    }
    /**
     * Generate a valid {@link Browser}.
     */
    public Browser aBrowser() {
        return Browser.BrowserBuilder.aBrowser()
                .withLocale(Locale.getDefault())
                .withIp("192.168.0.1")
                .withUserAgent(aUserAgent())
                .build();
    }
    /**
     * Generate a valid {@link PaymentFormContext}.
     */
    public PaymentFormContext aPaymentFormContext() {
        Map<String, String> paymentFormParameter = new HashMap<>();

        return PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext()
                .withPaymentFormParameter(paymentFormParameter)
                .withSensitivePaymentFormParameter(new HashMap<>())
                .build();
    }
    /**
     * @return a valid user agent.
     */
    public String aUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0";
    }
    public TransactionStatusRequest.TransactionStatusRequestBuilder aPaylineTransactionStatusRequestBuilder(){
        return TransactionStatusRequest.TransactionStatusRequestBuilder
                .aNotificationRequest()
                .withTransactionId(TRANSACTION_ID)
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withAmount(aPaylineAmount())
                .withBuyer(aBuyer())
                .withOrder(aPaylineOrder());

    }

    public TransactionStatusRequest aPaylineTransactionStatusRequest(){
        return aPaylineTransactionStatusRequestBuilder().build();
    }

    /**
     * Generate a valid {@link PaymentFormConfigurationRequest}.
     */
    public PaymentFormConfigurationRequest aPaymentFormConfigurationRequest() {
        return aPaymentFormConfigurationRequestBuilder().build();
    }
    /**
     * Generate a builder for a valid {@link PaymentFormConfigurationRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder aPaymentFormConfigurationRequestBuilder() {
        return PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withAmount(aPaylineAmount())
                .withBuyer(aBuyer())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withLocale(Locale.FRANCE)
                .withOrder(aPaylineOrder())
                .withPartnerConfiguration(aPartnerConfiguration());
    }
}