package com.brennaswitzer.cookbook.util;

public class ValueUtils {

    public static boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean noValue(String s) {
        return s == null || s.isBlank();
    }

}
