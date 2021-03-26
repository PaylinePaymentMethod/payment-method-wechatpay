package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.nested.TradeState;
import com.payline.payment.wechatpay.bean.request.QueryOrderRequest;
import com.payline.payment.wechatpay.bean.response.NotificationMessage;
import com.payline.payment.wechatpay.bean.response.QueryOrderResponse;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.payment.wechatpay.service.RequestConfigurationService;
import com.payline.payment.wechatpay.util.Converter;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.XMLService;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.payment.wechatpay.util.security.SignatureUtil;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.TransactionCorrelationId;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.BuyerPaymentId;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.NotificationService;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class NotificationServiceImpl implements NotificationService {
    private Converter converter = Converter.getInstance();
    private XMLService xmlService = XMLService.getInstance();
    private HttpService httpService = HttpService.getInstance();
    private SignatureUtil signatureUtil = SignatureUtil.getInstance();

    private static final String HTTP_BODY = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

    @Override
    public NotificationResponse parse(NotificationRequest notificationRequest) {
        // init data
        PaymentResponse paymentResponse;
        NotificationResponse notificationResponse;
        String partnerTransactionId = "UNKNOWN";

        try {
            // prepare data
            RequestConfiguration configuration = RequestConfigurationService.getInstance().build(notificationRequest);

            // get notification data
            String notificationMessage = PluginUtils.inputStreamToString(notificationRequest.getContent());

            Map<String, String> mNotificationMessage = xmlService.xmlToMap(notificationMessage);

            checkSignature(configuration, notificationMessage, mNotificationMessage);

            NotificationMessage message = converter.mapToObject(mNotificationMessage, NotificationMessage.class);
            String transactionId = message.getTransactionId();

            // call WeChatPay API to get the transaction status
            QueryOrderRequest queryOrderRequest = QueryOrderRequest.builder()
                    .appId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.APPID))
                    .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                    .subAppId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SUB_APPID))
                    .subMerchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.SUB_MERCHANT_ID).getValue())
                    .deviceInfo(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.DEVICE_INFO))
                    .transactionId(transactionId)
                    .nonceStr(PluginUtils.generateRandomString(32))
                    .signType(SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE)).getType())
                    .build();

            QueryOrderResponse queryOrderResponse = httpService.queryOrder(configuration, queryOrderRequest);

            // check transaction status
            TradeState tradeState = queryOrderResponse.getTradeState();
            partnerTransactionId = queryOrderResponse.getTransactionId();
            BuyerPaymentId buyerPaymentId = new EmptyTransactionDetails();

            if (tradeState == TradeState.SUCCESS) {
                paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                        .aPaymentResponseSuccess()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withStatusCode(tradeState.name())
                        .withTransactionDetails(buyerPaymentId)
                        .build();
            } else {
                paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withErrorCode(queryOrderResponse.getErrorCode())
                        .withFailureCause(FailureCause.PARTNER_UNKNOWN_ERROR)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
            }
        } catch (PluginException e) {
            log.info("a PluginException occurred", e);
            paymentResponse = e.toPaymentResponseFailureBuilder()
                    .withPartnerTransactionId(partnerTransactionId)
                    .build();

        } catch (RuntimeException e) {
            log.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withErrorCode(PluginUtils.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

        TransactionCorrelationId correlationId = TransactionCorrelationId.TransactionCorrelationIdBuilder
                .aCorrelationIdBuilder()
                .withType(TransactionCorrelationId.CorrelationIdType.PARTNER_TRANSACTION_ID)
                .withValue(partnerTransactionId)
                .build();

        notificationResponse = PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder
                .aPaymentResponseByNotificationResponseBuilder()
                .withPaymentResponse(paymentResponse)
                .withHttpBody(HTTP_BODY)
                .withHttpStatus(200)
                .withTransactionCorrelationId(correlationId)
                .build();

        return notificationResponse;
    }

    private void checkSignature(final RequestConfiguration configuration, final String notificationMessage, final Map<String, String> mNotificationMessage) {
        // verify Signature
        String key = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.KEY);
        SignType signType = SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE));
        if (!signatureUtil.isSignatureValid(mNotificationMessage, key, signType.getType())) {
            log.error("Invalid sign value in XML: {}", notificationMessage);
            throw new PluginException("Invalid signature", FailureCause.INVALID_DATA);
        }
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        // does nothing
    }
}
