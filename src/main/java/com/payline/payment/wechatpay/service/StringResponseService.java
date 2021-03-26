package com.payline.payment.wechatpay.service;

import com.payline.payment.wechatpay.util.http.StringResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class StringResponseService {

    private static class Holder {
        private static final StringResponseService INSTANCE = new StringResponseService();
    }

    public static StringResponseService getInstance() {
        return StringResponseService.Holder.INSTANCE;
    }

    /**
     * Safely extract the elements of a {@link StringResponse} from a {@link HttpResponse}.
     *
     * @param httpResponse the HTTP response
     * @return The corresponding StringResponse, or null if the input cannot be read or contains incomplete data.
     */
    public StringResponse fromHttpResponse(final HttpResponse httpResponse) {
        final StringResponse stringResponse;

        if (httpResponse != null && httpResponse.getStatusLine() != null) {
            final StringResponse.StringResponseBuilder builder = StringResponse.builder();
            builder.statusCode(httpResponse.getStatusLine().getStatusCode());
            builder.statusMessage(httpResponse.getStatusLine().getReasonPhrase());

            if (httpResponse.getEntity() != null) {
                try {
                    builder.content(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
                } catch (IOException e) {
                    log.error("Unable to read http response content, setting content to null");
                }
            }

            final Map<String, String> headers = new HashMap<>();
            final Header[] rawHeaders = httpResponse.getAllHeaders();
            for (Header rawHeader : rawHeaders) {
                headers.put(rawHeader.getName().toLowerCase(), rawHeader.getValue());
            }
            builder.headers(headers);
            stringResponse = builder.build();
        } else {
            stringResponse = null;
        }
        return stringResponse;
    }
}
