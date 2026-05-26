package com.tyfanchz.dynamicscript.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    }

    private JsonUtils() {}

    public static String toJson(Object object) {
        String json;

        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            json = null;
            e.printStackTrace();
        }

        return json;
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        T object;

        try {
            object = mapper.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            object = null;
            e.printStackTrace();
        }

        return object;
    }

    public static <T> T fromJson(String json, TypeReference<T> typeOfT) {
        T object;

        try {
            object = mapper.readValue(json, typeOfT);
        } catch (JsonProcessingException e) {
            object = null;
            e.printStackTrace();
        }

        return object;
    }
}
