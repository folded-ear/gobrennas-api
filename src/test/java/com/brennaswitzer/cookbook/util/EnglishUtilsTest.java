package com.brennaswitzer.cookbook.util;

import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.EnglishUtils.canonicalize;
import static com.brennaswitzer.cookbook.util.EnglishUtils.unpluralize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnglishUtilsTest {

    @Test
    public void testUnpluralize() {
        assertEquals("cup",      unpluralize("cup"));
        assertEquals("cup",      unpluralize("cups"));
        assertEquals("patty",    unpluralize("patties"));
        assertEquals("tomato",   unpluralize("tomatoes"));
        assertEquals("potato",   unpluralize("potatoes"));
        assertEquals("ounce",    unpluralize("ounces"));
        assertEquals("liter",    unpluralize("liters"));
        assertEquals("litre",    unpluralize("litres"));
        assertEquals("carnitas", unpluralize("carnitas"));
        assertEquals("apple",    unpluralize("apples"));
    }

    @Test
    public void testCanonicalize() {
        assertEquals("egg", canonicalize("egg"));
        assertEquals("egg", canonicalize("egg,"));
        assertEquals("egg", canonicalize(",egg"));
        assertEquals("egg", canonicalize(",egg,"));
        assertEquals("egg", canonicalize(" egg , "));
        assertEquals("egg", canonicalize(" , egg "));
        assertEquals("egg", canonicalize(" , egg , "));
    }

}
