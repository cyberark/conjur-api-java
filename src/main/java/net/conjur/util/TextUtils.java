package net.conjur.util;

import java.io.UnsupportedEncodingException;


/**
 * String helpers.  Borrows heavily from apache commons text helpers.
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

    /**
     * True if {@link TextUtils#isEmpty(CharSequence)} is true for {@code s}
     * or {@code s} contains only whitespace characters as determined by
     * {@link Character#isWhitespace(char)}.
     *
     * @param s the string to test
     * @return whether it's blank.
     */
    public static boolean isBlank(final CharSequence s) {
        if(isEmpty(s)) {
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
