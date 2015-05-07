package net.conjur.util;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 */
public class JsonSupport {
    public static String toJson(Object object){
       return new Gson().toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type){
        return new Gson().fromJson(json, type);
    }

    public static <T> T fromJson(Reader json, Class<T> type){
        return new Gson().fromJson(json, type);
    }


    public static <T> T fromJson(InputStream json, Class<T> type){
        final Reader reader = new BufferedReader(new InputStreamReader(json));
        return fromJson(reader, type);
    }

}
