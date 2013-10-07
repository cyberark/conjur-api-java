package net.conjur.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Helper to encode URI components without checked exceptions
 */
public class EncodeUriComponent {
    public static String encodeUriComponent(String uriComponent){
        try {
            return URLEncoder.encode(uriComponent, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("WTF UTF-8 encoding is not supported?");
        }
    }
}
