package com.brennaswitzer.cookbook.util;

import java.util.regex.Pattern;

public final class EnglishUtils {

    private static Pattern PLURAL_PATTERN = Pattern.compile(".*[^ai]s$");
    private static Pattern SPACES = Pattern.compile("  +");
    private static Pattern END_PUNCT = Pattern.compile("(^[^a-zA-Z0-9]+)|([^a-zA-Z0-9]+$)");

    public static String unpluralize(String word) {
        if (word == null) return word;
        // hardly comprehensive, but it does "eggs", "carnitas", and "ounces"
        if (PLURAL_PATTERN.matcher(word).matches()) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    public static String canonicalize(String s) {
        if (s == null) return s;
        s = SPACES.matcher(s).replaceAll(" ");
        s = END_PUNCT.matcher(s).replaceAll("");
        s = s.trim();
        return s;
    }

}
