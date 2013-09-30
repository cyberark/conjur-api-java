package net.conjur.util;

/**
 *
 */
public class Lang {
    public static <T> T getOrElse(T value, T orElse){
        return value == null ? orElse : value;
    }
}
