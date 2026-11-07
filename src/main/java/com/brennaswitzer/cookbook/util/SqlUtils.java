package com.brennaswitzer.cookbook.util;

import java.util.regex.Pattern;

public class SqlUtils {

    private static final Pattern LIKE_METACHARACTER = Pattern.compile("[\\\\%_]");

    /**
     * I escape LIKE metacharacters ({@code %}, {@code _}, and the escape
     * character {@code \} itself) so the passed value matches literally in a
     * {@code like ... escape '\'} expression.
     */
    public static String escapeLike(String value) {
        if (value == null) return null;
        return LIKE_METACHARACTER.matcher(value)
                .replaceAll("\\\\$0");
    }

}
