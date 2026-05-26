package com.tyfanchz.java.stuspringbootaop.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * <p>Description:
 * <p>Project: stu-spring-boot-aop
 *
 * @author wbh
 * @date 2020-12-09
 */
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String toJson(T obj) {
        String json;

        try {
            json = mapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            json = "";
        }

        return json;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        T obj;

        try {
            obj = mapper.readValue(json, clazz);
        } catch (Exception e) {
            obj = null;
            e.printStackTrace();
        }

        return obj;
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        T obj;

        try {
            obj = mapper.readValue(json, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
            obj = null;
        }

        return obj;
    }
}
