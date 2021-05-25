package com.payline.payment.wechatpay.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.payline.payment.wechatpay.bean.configuration.Acquirer;
import com.payline.payment.wechatpay.exception.PluginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

public class AcquirerService {

    private static class Holder {
        private static final AcquirerService INSTANCE = new AcquirerService();
    }

    public static AcquirerService getInstance() {
        return AcquirerService.Holder.INSTANCE;
    }


    /**
     * Retrieves the acquirers from pluginConfiguration
     * @param pluginConfiguration the plugin configuration
     * @return the acquirers
     * @throws PluginException if the acquirers cannot be retrieved from the pluginConfiguration
     */
    public List<Acquirer> retrieveAcquirers(final String pluginConfiguration) {
        final List<Acquirer> acquirers;
        if (pluginConfiguration != null) {
            try {
                final ObjectMapper objectMapper = new ObjectMapper();
                acquirers = objectMapper.readValue(pluginConfiguration, new TypeReference<List<Acquirer>>() { });
            } catch (final IOException e) {
                throw new PluginException("Unable to deserialize acquirers from pluginConfiguration", e);
            }
        } else {
            throw new PluginException("Unable to deserialize acquirers : pluginConfiguration is not set");
        }
        return acquirers;
    }

    /**
     * Retrieves an acquirer from pluginConfiguration
     * @param pluginConfiguration from pluginConfiguration
     * @param acquirerId the acquirer id to find
     * @return the acquirer if found
     * @throws PluginException if the acquirer is not found
     */
    public Acquirer fetchAcquirer(final String pluginConfiguration, final String acquirerId) {
        return retrieveAcquirers(pluginConfiguration)
                .stream()
                .filter(acquirer ->acquirerId.equalsIgnoreCase(acquirer.getId()))
                .findFirst()
                .orElseThrow(() -> new PluginException("acquirer not found [ID] : " + acquirerId));
    }
}
