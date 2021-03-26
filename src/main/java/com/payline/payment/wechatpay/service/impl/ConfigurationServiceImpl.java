package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.bean.configuration.RequestConfiguration;
import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.nested.SignType;
import com.payline.payment.wechatpay.bean.request.DownloadTransactionHistoryRequest;
import com.payline.payment.wechatpay.bean.response.Response;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.payment.wechatpay.service.RequestConfigurationService;
import com.payline.payment.wechatpay.util.PluginUtils;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.payment.wechatpay.util.constant.PartnerConfigurationKeys;
import com.payline.payment.wechatpay.util.i18n.I18nService;
import com.payline.payment.wechatpay.util.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;

@Log4j2
public class ConfigurationServiceImpl implements ConfigurationService {
    private HttpService httpService = HttpService.getInstance();
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

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        final Map<String, String> errors = new HashMap<>();
        try {
            // check il all mandatory fields are filled
            Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
            Locale locale = contractParametersCheckRequest.getLocale();

            // check required fields
            for (AbstractParameter param : this.getParameters(locale)) {
                if (param.isRequired() && accountInfo.get(param.getKey()) == null) {
                    log.info("contract param: {} is mandatory but missing", param.getKey());
                    String message = i18n.getMessage(I18N_CONTRACT_PREFIX + param.getKey() + ".requiredError", locale);
                    errors.put(param.getKey(), message);
                }
            }

            if (errors.isEmpty()) {
                final String errorMessage = checkWechatPayParameters(contractParametersCheckRequest);
                if (!PluginUtils.isEmpty(errorMessage)) {
                    errors.put(GENERIC_ERROR, errorMessage);
                }
            }
        } catch (PluginException e) {
            log.info("a PluginException occurred", e);
            errors.put(GENERIC_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected plugin error", e);
            errors.put(GENERIC_ERROR, e.getMessage());
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

    private String checkWechatPayParameters(final ContractParametersCheckRequest contractParametersCheckRequest) {
        final String errorMessage;
        // call for a getCheckoutSession with a bad checkoutSession id
        final RequestConfiguration configuration = RequestConfigurationService.getInstance().build(contractParametersCheckRequest);
        // call WechatPay API
        final DownloadTransactionHistoryRequest downloadTransactionHistoryRequest = DownloadTransactionHistoryRequest.builder()
                .appId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.APPID))
                .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                .subAppId(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SUB_APPID))
                .subMerchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.SUB_MERCHANT_ID).getValue())
                .nonceStr(PluginUtils.generateRandomString(32))
                .signType(SignType.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SIGN_TYPE)).getType())
                .billDate(PluginUtils.createDate())
                .billType("ALL")
                .build();

        final Response response = httpService.downloadTransactionHistory(configuration, downloadTransactionHistoryRequest);
        // verify Response
        final Code code = response.getResultCode();
        final String errorCode = response.getErrorCode();
        if (code.equals(Code.FAIL) && !errorCode.equals("20002")) {
            errorMessage = response.getReturnMessage();
        } else {
            errorMessage = null;
        }
        return errorMessage;
    }

}