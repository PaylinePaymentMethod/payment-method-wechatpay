package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.Refund;
import com.payline.payment.wechatpay.bean.nested.RefundStatus;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.nested.TradeState;
import com.payline.payment.wechatpay.bean.request.QueryOrderRequest;
import com.payline.payment.wechatpay.bean.request.QueryRefundRequest;
import com.payline.payment.wechatpay.bean.response.QueryOrderResponse;
import com.payline.payment.wechatpay.bean.response.QueryRefundResponse;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.payment.wechatpay.service.RequestConfigurationService;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.BuyerPaymentId;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import lombok.extern.log4j.Log4j2;

import java.sql.Ref;

@Log4j2
public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {
    HttpService httpService = HttpService.getInstance();

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        // todo on peut return null ici?
        return null;
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        PaymentResponse paymentResponse;
        try {
            RequestConfiguration configuration = RequestConfigurationService.getInstance().build(transactionStatusRequest);

            // get the scenario
            String partnerTransactionId = transactionStatusRequest.getTransactionId();
            if (partnerTransactionId.startsWith("REFUND")){
                // refund scenario
                log.info("getting refund status");
                return getRefundStatus(transactionStatusRequest, configuration);
            }else{
                // payment scenario
                log.info("getting payment status");
                return getPaymentStatus(transactionStatusRequest, configuration);
            }
        } catch (PluginException e) {
            log.info("a PluginException occurred", e);
            paymentResponse = e.toPaymentResponseFailureBuilder().build();

        } catch (RuntimeException e) {
            log.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginUtils.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

        return paymentResponse;
    }


    public PaymentResponse getRefundStatus(TransactionStatusRequest request, RequestConfiguration configuration){
        PaymentResponse paymentResponse;
        String refundId = request.getTransactionId().replace("REFUND", "");

        QueryRefundRequest queryRefundRequest = QueryRefundRequest.builder()
                .appId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.APPID))
                .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                .subAppId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SUB_APPID))
                .subMerchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.SUB_MERCHANT_ID).getValue())
                .nonceStr(PluginUtils.generateRandomString(32))
                .signType(SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE)).getType())
                .refundId(refundId)
                .build();

        // get refund status
        QueryRefundResponse queryRefundResponse = httpService.queryRefund(configuration, queryRefundRequest);
        BuyerPaymentId buyerPaymentId = new EmptyTransactionDetails();

        Refund refund = queryRefundResponse.getRefunds().stream()
                .filter(r -> r.getRefundId().equals(refundId))
                .findFirst()
                .orElse(Refund.builder().refundStatus(RefundStatus.EMPTY).build());

        // create RefundResponse from refund status
        RefundStatus refundStatus = refund.getRefundStatus();

        switch (refundStatus) {
            case SUCCESS:
                paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                        .aPaymentResponseSuccess()
                        .withPartnerTransactionId(refundId)
                        .withStatusCode(refundStatus.name())
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;
            case PROCESSING:
                paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                        .aPaymentResponseSuccess()
                        .withPartnerTransactionId(refundId)
                        .withStatusCode("PENDING")
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;
            case REFUNDCLOSE:
                paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(refundId)
                        .withErrorCode(refundStatus.name())
                        .withFailureCause(FailureCause.REFUSED)
                        .build();
                break;
            default:
                paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(refundId)
                        .withErrorCode(refundStatus.name())
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .build();
        }

        return paymentResponse;
    }

    public PaymentResponse getPaymentStatus(TransactionStatusRequest request, RequestConfiguration configuration){
    PaymentResponse paymentResponse;

        QueryOrderRequest queryOrderRequest = QueryOrderRequest.builder()
                .appId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.APPID))
                .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                .subAppId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SUB_APPID))
                .subMerchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.SUB_MERCHANT_ID).getValue())
                .deviceInfo(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.DEVICE_INFO))
                .transactionId(request.getTransactionId())
                .nonceStr(PluginUtils.generateRandomString(32))
                .signType(SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE)).getType())
                .build();

        QueryOrderResponse response = httpService.queryOrder(configuration, queryOrderRequest);

        String partnerTransactionId = response.getTransactionId();
        BuyerPaymentId buyerPaymentId = new EmptyTransactionDetails();
        TradeState tradeState = response.getTradeState();
        switch (tradeState) {
            case SUCCESS:
                paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                        .aPaymentResponseSuccess()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withStatusCode(tradeState.name())
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;
            case NOTPAY:
            case CLOSED:
            case REVOKED:
                paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withErrorCode(response.getErrorCode())
                        .withFailureCause(FailureCause.CANCEL)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;

            default:
                paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withErrorCode(response.getErrorCode())
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
        }

        return paymentResponse;
    }
}
