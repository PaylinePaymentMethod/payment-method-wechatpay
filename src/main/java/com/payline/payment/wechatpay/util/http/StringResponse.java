package com.payline.payment.wechatpay.util.http;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple POJO supporting the core elements of an HTTP response, in a more readable format (especially the content).
 */
@Value
@Builder
public class StringResponse {
    int statusCode;
    Map<String, String> headers;
    String content;
    String statusMessage;
}

