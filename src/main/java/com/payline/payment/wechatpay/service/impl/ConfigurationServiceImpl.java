package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.i18n.I18nService;
import com.payline.payment.wechatpay.util.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Log4j2
public class ConfigurationServiceImpl implements ConfigurationService {
    private ReleaseProperties releaseProperties = ReleaseProperties.getInstance();
    private final I18nService i18n = I18nService.getInstance();

    private static final String I18N_CONTRACT_PREFIX = "contract.";

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // merchant id
        parameters.add(buildInputParameter(ContractConfigurationKeys.MERCHANT_ID, true, locale));

        // sub merchant id
        parameters.add(buildInputParameter(ContractConfigurationKeys.SUB_MERCHANT_ID, true, locale));
        // MERCHANT BANK CODE
        parameters.add(buildInputParameter(ContractConfigurationKeys.MERCHANT_BANK_CODE, true, locale));
        // NUM CONTRACT WECHAT
        parameters.add(buildInputParameter(ContractConfigurationKeys.NUM_CONTRACT_WECHAT, true, locale));

        final ListBoxParameter partnerTransactionIdListBoxParameter = new ListBoxParameter();
        partnerTransactionIdListBoxParameter.setKey(ContractConfigurationKeys.PARTNER_TRANSACTION_ID);
        partnerTransactionIdListBoxParameter.setLabel(i18n.getMessage("contract.partnerTransactionId.label", locale));
        partnerTransactionIdListBoxParameter.setDescription(i18n.getMessage("contract.partnerTransactionId.description", locale));
        final Map<String, String> partnerTransactionIdMap = new HashMap<>();
        for (final PartnerTransactionIdOptions partnerTransactionIdOption : PartnerTransactionIdOptions.values()) {
            partnerTransactionIdMap.put(partnerTransactionIdOption.name(), i18n.getMessage(partnerTransactionIdOption.getI18nKey(), locale));
        }
        partnerTransactionIdListBoxParameter.setList(partnerTransactionIdMap);
        partnerTransactionIdListBoxParameter.setRequired(true);
        parameters.add(partnerTransactionIdListBoxParameter);
        // TERMINAL NUMBER
        parameters.add(buildInputParameter(ContractConfigurationKeys.TERMINAL_NUMBER, true, locale));
        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        final Map<String, String> errors = new HashMap<>();
        // check il all mandatory fields are filled
        Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
        Locale locale = contractParametersCheckRequest.getLocale();

        // check required fields
        for (AbstractParameter param : this.getParameters(locale)) {
            if (param.isRequired() && PluginUtils.isEmpty(accountInfo.get(param.getKey()))) {
                log.info("contract param: {} is mandatory but missing", param.getKey());
                String message = i18n.getMessage(I18N_CONTRACT_PREFIX + param.getKey() + ".requiredError", locale);
                errors.put(param.getKey(), message);
            }
        }

        return errors;
    }

    @Override
    public ReleaseInformation getReleaseInformation() {
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(LocalDate.parse(releaseProperties.get("release.date"), DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .withVersion(releaseProperties.get("release.version"))
                .build();
    }

    @Override
    public String getName(Locale locale) {
        return i18n.getMessage("paymentMethod.name", locale);
    }

    /**
     * Build and return a new <code>InputParameter</code> for the contract configuration.
     *
     * @param key      The parameter key
     * @param required Is this parameter required ?
     * @param locale   The current locale
     * @return The new input parameter
     */
    private InputParameter buildInputParameter(String key, boolean required, Locale locale) {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setKey(key);
        inputParameter.setLabel(i18n.getMessage(I18N_CONTRACT_PREFIX + key + ".label", locale));
        inputParameter.setDescription(i18n.getMessage(I18N_CONTRACT_PREFIX + key + ".description", locale));
        inputParameter.setRequired(required);
        return inputParameter;
    }

}