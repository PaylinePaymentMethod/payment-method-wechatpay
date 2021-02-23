package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.Refund;
import com.payline.payment.wechatpay.bean.nested.RefundStatus;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.request.QueryRefundRequest;
import com.payline.payment.wechatpay.bean.request.SubmitRefundRequest;
import com.payline.payment.wechatpay.bean.response.QueryRefundResponse;
import com.payline.payment.wechatpay.bean.response.SubmitRefundResponse;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.payment.wechatpay.service.RequestConfigurationService;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.RefundService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RefundServiceImpl implements RefundService {
    HttpService httpService = HttpService.getInstance();

    @Override
    public RefundResponse refundRequest(RefundRequest refundRequest) {
        RefundResponse response;
        String refundId = "UNKNOWN";

        try {
            RequestConfiguration configuration = RequestConfigurationService.getInstance().build(refundRequest);

            // create refund
            SubmitRefundRequest submitRefundRequest = SubmitRefundRequest.builder()
                    .appId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.APPID))
                    .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                    .subAppId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SUB_APPID))
                    .subMerchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.SUB_MERCHANT_ID).getValue())
                    .nonceStr(PluginUtils.generateRandomString(32))
                    .signType(SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE)).getType())

                    .transactionId(refundRequest.getPartnerTransactionId())
                    .outTradeNo(refundRequest.getTransactionId())
                    .totalFee(refundRequest.getAmount().getAmountInSmallestUnit().toString())
                    .refundFee(refundRequest.getAmount().getAmountInSmallestUnit().toString())
                    .refundFeeType(refundRequest.getAmount().getCurrency().getCurrencyCode())
                    .build();

            SubmitRefundResponse submitRefundResponse = httpService.submitRefund(configuration, submitRefundRequest);
            refundId = submitRefundResponse.getRefundId();

            // ask for refund status
            QueryRefundRequest queryRefundRequest = QueryRefundRequest.builder()
                    .appId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.APPID))
                    .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                    .subAppId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SUB_APPID))
                    .subMerchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.SUB_MERCHANT_ID).getValue())
                    .nonceStr(PluginUtils.generateRandomString(32))
                    .signType(SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE)).getType())
                    .refundId(refundId)
                    .build();

            QueryRefundResponse queryRefundResponse = httpService.queryRefund(configuration, queryRefundRequest);

            // create RefundResponse from refund status
            Refund refund = queryRefundResponse.getRefunds().stream()
                    .filter(r -> submitRefundResponse.getRefundId().equals(r.getRefundId()))
                    .findAny()
                    .orElse(Refund.builder().refundStatus(RefundStatus.EMPTY).build());

            RefundStatus refundStatus = refund.getRefundStatus();
            switch (refundStatus) {
                case SUCCESS:
                    response = RefundResponseSuccess.RefundResponseSuccessBuilder
                            .aRefundResponseSuccess()
                            .withPartnerTransactionId(refundId)
                            .withStatusCode(refundStatus.name())
                            .build();
                    break;
                case PROCESSING:
                    response = RefundResponseSuccess.RefundResponseSuccessBuilder
                            .aRefundResponseSuccess()
                            .withPartnerTransactionId(refundId)
                            .withStatusCode("PENDING")
                            .build();
                    break;
                case REFUNDCLOSE:
                    response = RefundResponseFailure.RefundResponseFailureBuilder
                            .aRefundResponseFailure()
                            .withPartnerTransactionId(refundId)
                            .withErrorCode(refundStatus.name())
                            .withFailureCause(FailureCause.REFUSED)
                            .build();
                    break;
                default:
                    response = RefundResponseFailure.RefundResponseFailureBuilder
                            .aRefundResponseFailure()
                            .withPartnerTransactionId(refundId)
                            .withErrorCode(refundStatus.name())
                            .withFailureCause(FailureCause.INVALID_DATA)
                            .build();
            }

        } catch (PluginException e) {
            log.info("a PluginException occurred", e);
            response = e.toRefundResponseFailureBuilder().withPartnerTransactionId(refundId).build();

        } catch (RuntimeException e) {
            log.error("Unexpected plugin error", e);
            response = RefundResponseFailure.RefundResponseFailureBuilder
                    .aRefundResponseFailure()
                    .withPartnerTransactionId(refundId)
                    .withErrorCode(PluginUtils.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

        return response;
    }

    @Override
    public boolean canMultiple() {
        return true;
    }

    @Override
    public boolean canPartial() {
        return true;
    }
}
