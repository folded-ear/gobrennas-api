package com.brennaswitzer.cookbook.util;

import java.util.regex.Pattern;

public final class EnglishUtils {

    private static Pattern PLURAL_PATTERN = Pattern.compile("[^ai]s$");

    public static String unpluralize(String word) {
        if (word == null) return word;
        // hardly comprehensive, but it does "eggs", "carnitas", and "ounces"
        if (PLURAL_PATTERN.matcher(word).matches()) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

}
