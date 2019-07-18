package com.brennaswitzer.cookbook.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdUtils {

    private static final AtomicInteger id_seq = new AtomicInteger(0);

    public static Long next(Class clazz) {
        long t = System.currentTimeMillis() / 1000;
        long c = clazz.getName().hashCode();
        int s = id_seq.getAndIncrement();
        t <<= 32; // 32 bits of epoch time
        c = (c & 0xFFFF) << 16; // 16 low-order bits of class name
        s &= 0xFFFF; // 16 low-order bits of sequence
        return t | c | s;
    }

}
