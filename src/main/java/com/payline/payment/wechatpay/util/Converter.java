package com.payline.payment.wechatpay.util;

import com.payline.payment.wechatpay.bean.nested.Refund;
import com.payline.payment.wechatpay.bean.response.QueryRefundResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Converter {
    JsonService json = JsonService.getInstance();
    XMLService xml = XMLService.getInstance();

    private static class Holder {
        private static final Converter instance = new Converter();
    }

    public static Converter getInstance() {
        return Converter.Holder.instance;
    }

    /**
     * convert a bean to a map
     *
     * @param o the object to convert
     * @return a map with field name as key and fields value as value
     */
    public Map<String, String> objectToMap(Object o) {
        return json.fromJson(json.toJson(o), Map.class);
    }

    /**
     * convert a Map into a bean
     *
     * @param map   the map to convert
     * @param clazz the bean class to create
     * @param <T>
     * @return
     */
    public <T> T mapToObject(Map<String, String> map, Class<T> clazz) {
        return json.fromJson(json.toJson(map), clazz);
    }

    /**
     * Convert an XML String
     *
     * @param s     the XML String to convert
     * @param clazz the bean class
     * @param <T>
     * @return
     */
    public <T> T xmlToObject(String s, Class<T> clazz) {
        return mapToObject(xml.xmlToMap(s), clazz);
    }

    public QueryRefundResponse createQueryResponse(String s) {
        Map<String, String> m = xml.xmlToMap(s);
        QueryRefundResponse queryRefundResponse = mapToObject(m, QueryRefundResponse.class);

        List<Refund> refunds = new ArrayList<>();
        if(m.containsKey("refund_count")) {
            int count = Integer.parseInt(m.get("refund_count"));
            IntStream.range(0, count).forEach(i -> refunds.add(createRefund(i, m)));
            queryRefundResponse.setRefunds(refunds);
        }

        return queryRefundResponse;
    }

    public Map<String, String> xmlToMap(String s) {
        return xml.xmlToMap(s);
    }

    private Refund createRefund(final int i, Map<String,String> map){
        Map<String, String> refund = map.entrySet()
                .stream()
                .filter(e -> e.getKey().endsWith(String.valueOf(i)))
                .collect(Collectors.toMap(e -> e.getKey().replace("_"+i, ""), Map.Entry::getValue));

        return mapToObject(refund, Refund.class);
    }



}
