package com.payline.payment.wechatpay.util;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JsonService {
    Gson gson = new Gson();

    private static class Holder {
        private static final JsonService instance = new JsonService();
    }

    public static JsonService getInstance() {
        return JsonService.Holder.instance;
    }

    /**
     * convert a json String into a bean
     * @param json the String to convert
     * @param clazz the class to convert into
     * @param <T> the object returned
     * @return
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * convert a bean into a json String
     * @param o the object to convert
     * @return
     */
    public String toJson(Object o) {
        return gson.toJson(o);
    }
}
