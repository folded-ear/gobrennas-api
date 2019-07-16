package com.brennaswitzer.cookbook.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnglishUtilsTest {

    @Test
    public void unpluralize() {
        assertEquals("cup", EnglishUtils.unpluralize("cup"));
        assertEquals("cup", EnglishUtils.unpluralize("cups"));
    }
}