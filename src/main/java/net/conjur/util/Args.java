package net.conjur.util;
/*
 * Copied from org.apache.http.utils.Args to avoid dependencies.
 */

import java.util.Collection;

/**
 * Argument validation helpers.
 */
public class Args {

    private static final String NULL_ERROR = "may not be null";
    private static final String EMPTY_ERROR = "may not be empty";
    private static final String BLANK_ERROR = "may not be blank";
    private static final String POSITIVE_ERROR = "must be positive";
    private static final String NEGATIVE_ERROR = "may not be negative";

    public static void check(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void check(final boolean expression, final String message, final Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    public static <T> T notNull(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " " + NULL_ERROR);
        }
        return argument;
    }

    public static <T extends CharSequence> T notEmpty(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " " + NULL_ERROR);
        }
        if (TextUtils.isEmpty(argument)) {
            throw new IllegalArgumentException(name + " " + EMPTY_ERROR);
        }
        return argument;
    }

    public static <T extends CharSequence> T notBlank(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " " + NULL_ERROR);
        }
        if (TextUtils.isBlank(argument)) {
            throw new IllegalArgumentException(name + " " + BLANK_ERROR);
        }
        return argument;
    }

    public static <E, T extends Collection<E>> T notEmpty(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " " + NULL_ERROR);
        }
        if (argument.isEmpty()) {
            throw new IllegalArgumentException(name + " " + EMPTY_ERROR);
        }
        return argument;
    }

    public static int positive(final int n, final String name) {
        if (n <= 0) {
            throw new IllegalArgumentException(name + " " + POSITIVE_ERROR);
        }
        return n;
    }

    public static long positive(final long n, final String name) {
        if (n <= 0) {
            throw new IllegalArgumentException(name + " " + POSITIVE_ERROR);
        }
        return n;
    }

    public static int notNegative(final int n, final String name) {
        if (n < 0) {
            throw new IllegalArgumentException(name + " " + NEGATIVE_ERROR);
        }
        return n;
    }

    public static long notNegative(final long n, final String name) {
        if (n < 0) {
            throw new IllegalArgumentException(name + " " + NEGATIVE_ERROR);
        }
        return n;
    }

    public static <T> T notNull(T value) {
        return notNull(value, "argument");
    }
}

