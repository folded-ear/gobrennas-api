package com.brennaswitzer.cookbook.util;

import java.util.regex.Pattern;

public final class EnglishUtils {

    private static final Pattern SPACES = Pattern.compile("  +");
    private static final Pattern END_PUNCT = Pattern.compile("(^[^a-zA-Z0-9]+)|([^a-zA-Z0-9]+$)");

    public static String unpluralize(String word) {
        if (word == null) return word;
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + 'y';
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("as")) {
            return word;
        }
        if (word.endsWith("s")) {
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
