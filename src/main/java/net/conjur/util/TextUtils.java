package net.conjur.util;

import java.io.UnsupportedEncodingException;

/*
 * Copied from org.apache.http.utils.TextUtils to avoid dependencies.
 */

/**
 * A couple of String helpers.
 */
public final class TextUtils {

    public static String join(final CharSequence glue, final CharSequence[] parts){
        if(parts.length == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]);

        for(int i=1;i < parts.length; i++){
            sb.append(glue);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    public static boolean isEmpty(final CharSequence s) {
        if (s == null) {
            return true;
        }
        return s.length() == 0;
    }

    public static boolean isBlank(final CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Converts the byte array of characters to a string. If
     * the specified charset is not supported, default system encoding
     * is used.
     *
     * @param data the byte array to be encoded
     * @param offset the index of the first byte to encode
     * @param length the number of bytes to encode
     * @param charset the desired character encoding
     * @return The result of the conversion.
     */
    public static String getString(
        final byte[] data,
        final int offset,
        final int length,
        final String charset) {
        Args.notNull(data, "Input");
        Args.notEmpty(charset, "Charset");
        try {
            return new String(data, offset, length, charset);
        } catch (final UnsupportedEncodingException e) {
            return new String(data, offset, length);
        }
    }
    
    /**
     * Converts the specified string to a byte array.  If the charset is not supported the
     * default system charset is used.
     *
     * @param data the string to be encoded
     * @param charset the desired character encoding
     * @return The resulting byte array.
     */
    public static byte[] getBytes(final String data, final String charset) {
        Args.notNull(data, "Input");
        Args.notEmpty(charset, "Charset");
        try {
            return data.getBytes(charset);
        } catch (final UnsupportedEncodingException e) {
            return data.getBytes();
        }
    }
}
