package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Order;

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

    public String retrievePartnerTransactionId(final ContractConfiguration contractConfiguration, final String transactionId, final Order order) {
        final String partnerTransactionId;
        final ContractProperty contractProperty = contractConfiguration.getProperty(ContractConfigurationKeys.PARTNER_TRANSACTION_ID);
        if (contractProperty != null && PartnerTransactionIdOptions.EDEL.name().equals(contractProperty.getValue())) {
            partnerTransactionId = computeEdelPartnerTransactionId(contractConfiguration,transactionId);
        } else {
            partnerTransactionId = order.getReference();
        }

        return partnerTransactionId;
    }


    protected String computeEdelPartnerTransactionId(final ContractConfiguration contractConfiguration, final String transactionId ) {

        final String terminalNumberHex = decimalToHex(contractConfiguration.getProperty(ContractConfigurationKeys.TERMINAL_NUMBER).getValue());
        final String edelTransactionNumberDecimal = transactionId.substring(transactionId.length() - EDEL_TRANSACTION_NUMBER_SIZE);
        final String edelTransactionNumberHex = decimalToHex(edelTransactionNumberDecimal);

        final String bankCodeHex = decimalToHex(contractConfiguration.getProperty(ContractConfigurationKeys.MERCHANT_BANK_CODE).getValue());

        final String contractNumberHex = decimalToHex(contractConfiguration.getProperty(ContractConfigurationKeys.NUM_CONTRACT_WECHAT).getValue());

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
