package com.payline.payment.wechatpay.bean.configuration;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.exception.PluginException;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestConfigurationTest {
    private Environment environment = MockUtils.anEnvironment();
    private PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
    private ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
    private String pluginConfiguration =MockUtils.PLUGIN_CONFIGURATION;
    @Test
    void constructor_nominal(){
        // given: the constructor is passed valid arguments, when: calling the constructor
        RequestConfiguration requestConfiguration = new RequestConfiguration( contractConfiguration,
                environment, partnerConfiguration, pluginConfiguration );
        // then: the instance is not null, no exception is thrown
        assertNotNull( requestConfiguration );
    }

    @Test
    void constructor_nullContractConfiguration(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( null,
                environment, partnerConfiguration, pluginConfiguration) );
    }

    @Test
    void constructor_nullEnvironment(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( contractConfiguration,
                null,partnerConfiguration, pluginConfiguration) );
    }

    @Test
    void constructor_nullPartnerConfiguration(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( contractConfiguration,
                environment, null, pluginConfiguration ) );
    }

}
