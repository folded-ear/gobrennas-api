package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Identified;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdUtils {

    private static final AtomicInteger id_seq = new AtomicInteger(0);

    public static Long next(Class<?> clazz) {
        long t = System.currentTimeMillis() / 1000;
        long c = clazz.getName().hashCode();
        int s = id_seq.getAndIncrement();
        t <<= 32; // 32 bits of epoch time
        c = (c & 0xFFFF) << 16; // 16 low-order bits of class name
        s &= 0xFFFF; // 16 low-order bits of sequence
        return t | c | s;
    }

    public static long[] toIdList(Collection<? extends Identified> items) {
        long[] result = new long[items.size()];
        int i = 0;
        for (Identified it : items) {
            result[i++] = it.getId();
        }
        return result;
    }

}
