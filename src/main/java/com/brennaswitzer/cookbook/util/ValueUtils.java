package com.brennaswitzer.cookbook.util;

import java.util.Collection;
import java.util.Map;

public class ValueUtils {

    public static boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean hasValue(Collection<?> coll) {
        return coll != null && !coll.isEmpty();
    }

    public static boolean hasValue(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean noValue(String s) {
        return s == null || s.isBlank();
    }

    public static boolean noValue(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean noValue(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
