package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.MockUtils;
import com.payline.payment.wechatpay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.wechatpay.util.constant.ContractConfigurationKeys;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

 class PartnerTransactionIdServiceTest {
    @InjectMocks
    @Spy
    private PartnerTransactionIdService underTest ;

    @BeforeEach
    void setup() {
        underTest = PartnerTransactionIdService.getInstance();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void retreivePartnerTransactionIdEdel() {
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        paymentRequest.getContractConfiguration().getContractProperties()
                .put(ContractConfigurationKeys.PARTNER_TRANSACTION_ID, new ContractProperty(PartnerTransactionIdOptions.EDEL.name()));
        final String partnerTransactionId = "partnerTransactionId";

        doReturn(partnerTransactionId).when(underTest).computeEdelPartnerTransactionId(paymentRequest);

        final String result = underTest.retrievePartnerTransactionId(paymentRequest);

        assertEquals(partnerTransactionId, result);
    }

    @Test
    void retreivePartnerTransactionIdOrderReference() {
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        paymentRequest.getContractConfiguration().getContractProperties()
                .put(ContractConfigurationKeys.PARTNER_TRANSACTION_ID, new ContractProperty(PartnerTransactionIdOptions.ORDER_REFERENCE.name()));

        final String result = underTest.retrievePartnerTransactionId(paymentRequest);

        assertEquals(paymentRequest.getOrder().getReference(), result);
    }

    @Test
    void computeEdelPartnerTransactionId() {
        final String transactionIdDecimal = "G98765430051966";
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequestBuilder()
                .withTransactionId(transactionIdDecimal)
                .build();

        doReturn(LocalDateTime.of(2020, 12, 13, 14, 15, 16)).when(underTest).currentDateTime();

        final String partnerTransactionId = underTest.computeEdelPartnerTransactionId(paymentRequest);

        assertEquals("W000CAFE03039140ED80032ED93CE20C", partnerTransactionId);

        // W 000CAFE 03039 140ED8 003 2ED93CE20C

        final Long expectedTID = Long.parseLong(transactionIdDecimal.substring(transactionIdDecimal.length() - 7));
        final String tidSubstring = partnerTransactionId.substring(1, 8);
        assertEquals( "000CAFE",tidSubstring);
        assertEquals(expectedTID, hexToDec(tidSubstring));

        final Long expectedMerchantBankCode = 12345L;
        final String merchantBankCodeSubstring = partnerTransactionId.substring(8, 13);
        assertEquals( "03039",merchantBankCodeSubstring);
        assertEquals(expectedMerchantBankCode, hexToDec(merchantBankCodeSubstring));

        final Long expectedContractCode = 1314520L;
        final String contractCodeSubstring = partnerTransactionId.substring(13, 19);
        assertEquals( "140ED8",contractCodeSubstring);
        assertEquals(expectedContractCode, hexToDec(contractCodeSubstring));

        final String terminalSubstring = partnerTransactionId.substring(19, 22);
        assertEquals( "003",terminalSubstring);
        assertEquals(3L, hexToDec(terminalSubstring));

        final long expectedDate = 201213141516L;
        final String dateSubstring = partnerTransactionId.substring(22);
        assertEquals( "2ED93CE20C",dateSubstring);
        assertEquals(expectedDate, hexToDec(dateSubstring));
    }

    private Long hexToDec(final String hex) {
        return Long.parseLong(hex, 16);
    }
}
