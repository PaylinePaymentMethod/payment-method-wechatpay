package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PartnerTransactionIdService {

    public static final int EDEL_TRANSACTION_NUMBER_SIZE = 7;
    public static final int EDEL_BANK_CODE_SIZE = 5;
    public static final int EDEL_CONTRACT_NUMBER_SIZE = 6;
    public static final int EDEL_TERMINAL_NUMBER_SIZE = 3;
    public static final int EDEL_DATE_TIME_SIZE = 10;
    public static final String EDEL_DATE_TIME_PATTERN = "yyMMddHHmmss";
    public static final String WECHATPAY_TRANSACTION_ID_FLAG = "W";

    private static class Holder {
        private static final PartnerTransactionIdService INSTANCE = new PartnerTransactionIdService();
    }

    public static PartnerTransactionIdService getInstance() {
        return PartnerTransactionIdService.Holder.INSTANCE;
    }

    public String retrievePartnerTransactionId(final PaymentRequest paymentRequest) {
        final String partnerTransactionId;
        final ContractProperty contractProperty = paymentRequest.getContractConfiguration().getProperty(ContractConfigurationKeys.PARTNER_TRANSACTION_ID);
        if (contractProperty != null && PartnerTransactionIdOptions.EDEL.name().equals(contractProperty.getValue())) {
            partnerTransactionId = computeEdelPartnerTransactionId(paymentRequest);
        } else {
            partnerTransactionId = paymentRequest.getOrder().getReference();
        }

        return partnerTransactionId;
    }

    protected String computeEdelPartnerTransactionId(final PaymentRequest paymentRequest) {

        final String terminalNumber = paymentRequest.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.TERMINAL_NUMBER);
        if (terminalNumber == null) {
            throw new PluginException(PartnerConfigurationKeys.TERMINAL_NUMBER + " is missing in the PartnerConfiguration");
        }

        final ContractConfiguration contractConfiguration = paymentRequest.getContractConfiguration();
        final String terminalNumberHex = decimalToHex(terminalNumber);

        final String edelTransactionNumberDecimal = paymentRequest.getTransactionId().substring(paymentRequest.getTransactionId().length() - EDEL_TRANSACTION_NUMBER_SIZE);
        final String edelTransactionNumberHex = decimalToHex(edelTransactionNumberDecimal);

        final String bankCodeHex = decimalToHex(contractConfiguration.getProperty(ContractConfigurationKeys.MERCHANT_BANK_CODE).getValue());

        final String contractNumberHex = decimalToHex(contractConfiguration.getProperty(ContractConfigurationKeys.SECONDARY_MERCHANT_ID).getValue());

        final String dateTimeHex = decimalToHex(currentDateTime().format(DateTimeFormatter.ofPattern(EDEL_DATE_TIME_PATTERN)));

        return WECHATPAY_TRANSACTION_ID_FLAG + PluginUtils.leftPad(edelTransactionNumberHex, EDEL_TRANSACTION_NUMBER_SIZE)
                + PluginUtils.leftPad(bankCodeHex, EDEL_BANK_CODE_SIZE) + PluginUtils.leftPad(contractNumberHex, EDEL_CONTRACT_NUMBER_SIZE)
                + PluginUtils.leftPad(terminalNumberHex, EDEL_TERMINAL_NUMBER_SIZE) + PluginUtils.leftPad(dateTimeHex, EDEL_DATE_TIME_SIZE);
    }

    public String decimalToHex(final String decimalString) {
        return Long.toHexString(Long.parseLong(decimalString)).toUpperCase();
    }

    protected LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }
}
