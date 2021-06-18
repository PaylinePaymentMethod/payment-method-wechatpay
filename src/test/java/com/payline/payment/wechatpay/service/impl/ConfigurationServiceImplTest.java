package com.payline.payment.wechatpay.service.impl;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.bean.nested.Code;
import com.payline.payment.wechatpay.bean.response.Response;
import com.payline.payment.wechatpay.exception.InvalidDataException;
import com.payline.payment.wechatpay.service.HttpService;
import com.payline.payment.wechatpay.util.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;
import java.util.stream.Stream;

import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;


class ConfigurationServiceImplTest {

    @InjectMocks
    ConfigurationServiceImpl service = new ConfigurationServiceImpl();

    @Mock
    private ReleaseProperties releaseProperties;

    @Mock
    private HttpService httpService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Set of locales to test the getParameters() method. ZZ allows to search in the default messages.properties file.
     */
    static Stream<Locale> getLocales() {
        return Stream.of(Locale.FRENCH, Locale.ENGLISH, new Locale("BAD_LOCALE"));
    }


    @ParameterizedTest
    @MethodSource("getLocales")
    void getParameters(Locale locale) {
        List<AbstractParameter> parameters = service.getParameters(locale);

        assertEquals(6, parameters.size());

        for (AbstractParameter p : parameters) {
            // each parameter should have a label and a description
            assertNotNull(p.getLabel());
            assertFalse(p.getLabel().contains("???"));
            assertNotNull(p.getDescription());
            assertFalse(p.getDescription().contains("???"));

            // in case of a ListBoxParameter, it should have at least 1 value
            if (p instanceof ListBoxParameter) {
                assertFalse(((ListBoxParameter) p).getList().isEmpty());
            }
        }
    }

    @Test
    void checkNominal() {
        Response response = Response.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .resultCode(Code.FAIL)
                .errorCode("20002")
                .build();
        doReturn(response).when(httpService).downloadTransactionHistory(any(), any());

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void checkFail() {
        Response response = Response.builder()
                .appId("appId")
                .merchantId("mchId")
                .subMerchantId("subMchId")
                .nonceStr("123")
                .returnCode(Code.SUCCESS)
                .returnMessage("foo")
                .resultCode(Code.FAIL)
                .errorCode("20003")
                .build();
        doReturn(response).when(httpService).downloadTransactionHistory(any(), any());

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        assertEquals(1, errors.size());
        assertTrue( errors.containsKey(GENERIC_ERROR));
        assertEquals("foo", errors.get(GENERIC_ERROR));
    }


    @Test
    void checkPluginException(){
        Mockito.doThrow(new InvalidDataException("foo")).when(httpService).downloadTransactionHistory(any(), any());

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        assertEquals(1, errors.size());
        assertEquals("foo", errors.get(GENERIC_ERROR));
    }

    @Test
    void checkRuntimeException(){
        Mockito.doThrow(new NullPointerException("foo")).when(httpService).downloadTransactionHistory(any(), any());

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        assertEquals(1, errors.size());
        assertEquals("foo", errors.get(GENERIC_ERROR));
    }

    @Test
    void getReleaseInformation() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        doReturn(version).when(releaseProperties).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);
        doReturn(formatter.format(cal.getTime())).when(releaseProperties).get("release.date");

        // when: calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        assertEquals(version, releaseInformation.getVersion());
        assertEquals(2019, releaseInformation.getDate().getYear());
        assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }

    @Test
    void getName(){
        String name = "WeChatPay";
        assertEquals(name, service.getName(Locale.FRANCE));
        assertEquals(name, service.getName(Locale.ENGLISH));
        assertEquals(name, service.getName(Locale.CHINA));

    }
}