package net.conjur.util;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 *
 */
public class JsonSupport {
    public static String toJson(Object object){
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type){
        try {
            return new ObjectMapper().readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
