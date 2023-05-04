package com.brennaswitzer.cookbook.util;

import java.util.List;

import static java.util.Collections.emptyList;

public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("really?");
    }

    public static <E> E head(List<E> list) {
        int n = list.size();
        if (n < 1) throw new IllegalArgumentException("Cannot get the head of an empty list");
        return list.get(0);
    }

    public static <E> List<E> tail(List<E> list) {
        int n = list.size();
        if (n < 2) return emptyList();
        return list.subList(1, n);
    }

}
