package com.payline.payment.wechatpay.utils;

import com.payline.payment.wechatpay.util.http.StringResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringResponseTest {

    @Test
    void fromHttpResponse_nominal(){
        // given: a complete HTTP response
        CloseableHttpResponse httpResponse = HttpTestUtils.mockHttpResponse( 200, "OK", "some content",
                new Header[]{ new BasicHeader("Name", "Value")} );

        // when: converting it to StringResponse
        StringResponse stringResponse = StringResponse.fromHttpResponse( httpResponse );

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
        StringResponse stringResponse = StringResponse.fromHttpResponse( null );

        // then: the StringResponse is null
        assertNull( stringResponse );
    }

    @Test
    void toStringMethod(){
        // given: a StringResponse instance
        String jsonContent = "{\"message\":\"This is an error message\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Test", "This is a test header");
        StringResponse stringResponse = HttpTestUtils.mockStringResponse( 400, "Bad Request", jsonContent, headers);

        // when: calling toString method, then: the result is as expected
        String ln = System.lineSeparator();
        assertEquals( "HTTP 400 Bad Request" + ln
                        + "Test: This is a test header" + ln
                        + jsonContent
                , stringResponse.toString() );
    }

}
