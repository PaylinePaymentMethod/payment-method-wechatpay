package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.util.http.StringResponse;
import com.payline.payment.wechatpay.utils.HttpTestUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringResponseServiceTest {

    @Test
    void fromHttpResponse_nominal(){
        // given: a complete HTTP response
        CloseableHttpResponse httpResponse = HttpTestUtils.mockHttpResponse( 200, "OK", "some content",
                new Header[]{ new BasicHeader("Name", "Value")} );

        // when: converting it to StringResponse
        StringResponse stringResponse = StringResponseService.getInstance().fromHttpResponse( httpResponse );

        // then: the StringResponse attributes match the content of the HttpResponse
        assertNotNull( stringResponse );
        assertEquals( 200, stringResponse.getStatusCode() );
        assertEquals( "OK", stringResponse.getStatusMessage() );
        assertEquals( "some content", stringResponse.getContent() );
        assertEquals( 1, stringResponse.getHeaders().size() );

    }

    @Test
    void fromHttpResponse_null(){
        // when: converting null to StringResponse
        StringResponse stringResponse = StringResponseService.getInstance().fromHttpResponse( null );

        // then: the StringResponse is null
        assertNull( stringResponse );
    }
}
