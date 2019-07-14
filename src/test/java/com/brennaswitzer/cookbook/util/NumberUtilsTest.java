package com.brennaswitzer.cookbook.util;

import org.junit.Test;

import static com.brennaswitzer.cookbook.util.NumberUtils.parseFloat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NumberUtilsTest {

    @Test
    public void decimalFloat() {
        assertEquals(1f, parseFloat("1"), 0.001);
        assertEquals(1f, parseFloat("1.0"), 0.001);
        assertEquals(0.1f, parseFloat("0.1"), 0.001);
        assertEquals(0.01f, parseFloat("0.01"), 0.001);
        assertEquals(0.01f, parseFloat("0.01"), 0.001);
        assertEquals(1.23f, parseFloat("1.23"), 0.001);
    }

    @Test
    public void fractionFloat() {
        assertEquals(0.5f, parseFloat("1/2"), 0.001);
        assertEquals(0.5f, parseFloat("1/ 2"), 0.001);
        assertEquals(0.5f, parseFloat("1 /2"), 0.001);
        assertEquals(0.5f, parseFloat("1 / 2"), 0.001);

        assertEquals(0.333f, parseFloat("1/3"), 0.001);
        assertEquals(0.667f, parseFloat("2/3"), 0.001);
    }

    @Test
    public void fractionGlyphFloat() {
        assertEquals(0.25f, parseFloat("¼"), 0.001);
        assertEquals(0.5f, parseFloat("½"), 0.001);
        assertEquals(0.75f, parseFloat("¾"), 0.001);
        assertEquals(0.143f, parseFloat("⅐"), 0.001);
        assertEquals(0.111f, parseFloat("⅑"), 0.001);
        assertEquals(0.1f, parseFloat("⅒"), 0.001);
        assertEquals(0.333f, parseFloat("⅓"), 0.001);
        assertEquals(0.667f, parseFloat("⅔"), 0.001);
        assertEquals(0.2f, parseFloat("⅕"), 0.001);
        assertEquals(0.4f, parseFloat("⅖"), 0.001);
        assertEquals(0.6f, parseFloat("⅗"), 0.001);
        assertEquals(0.8f, parseFloat("⅘"), 0.001);
        assertEquals(0.167f, parseFloat("⅙"), 0.001);
        assertEquals(0.833f, parseFloat("⅚"), 0.001);
        assertEquals(0.125f, parseFloat("⅛"), 0.001);
        assertEquals(0.375f, parseFloat("⅜"), 0.001);
        assertEquals(0.625f, parseFloat("⅝"), 0.001);
        assertEquals(0.875f, parseFloat("⅞"), 0.001);
    }

    @Test
    public void properFractionFloat() {
        assertEquals(3.5f, parseFloat("3&1/2"), 0.001);
        assertEquals(3.5f, parseFloat("3 & 1/2"), 0.001);
        assertEquals(3.5f, parseFloat("3 and 1/2"), 0.001);
        assertEquals(3.5f, parseFloat("3 1/2"), 0.001);
        assertEquals(3.5f, parseFloat("3 ½"), 0.001);
        assertEquals(3.5f, parseFloat("3½"), 0.001);
        assertEquals(3.5f, parseFloat("3&½"), 0.001);
        assertEquals(3.5f, parseFloat("3 & ½"), 0.001);
        assertEquals(3.5f, parseFloat("3 and ½"), 0.001);
    }

    @Test
    public void nameFloat() {
        assertEquals(1f, parseFloat("one"), 0.001);
        assertEquals(2f, parseFloat("two"), 0.001);
        assertEquals(3f, parseFloat("three"), 0.001);
        assertEquals(4f, parseFloat("four"), 0.001);
        assertEquals(5f, parseFloat("five"), 0.001);
        assertEquals(6f, parseFloat("six"), 0.001);
        assertEquals(7f, parseFloat("seven"), 0.001);
        assertEquals(8f, parseFloat("eight"), 0.001);
        assertEquals(9f, parseFloat("nine"), 0.001);
        assertEquals(10f, parseFloat("ten"), 0.001);
        assertEquals(11f, parseFloat("eleven"), 0.001);
        assertEquals(12f, parseFloat("twelve"), 0.001);
        assertEquals(13f, parseFloat("thirteen"), 0.001);
        assertEquals(14f, parseFloat("fourteen"), 0.001);
        assertEquals(15f, parseFloat("fifteen"), 0.001);
        assertEquals(16f, parseFloat("sixteen"), 0.001);
        assertEquals(17f, parseFloat("seventeen"), 0.001);
        assertEquals(18f, parseFloat("eighteen"), 0.001);
        assertEquals(19f, parseFloat("nineteen"), 0.001);
        assertEquals(20f, parseFloat("twenty"), 0.001);

        assertEquals(0.5f, parseFloat("half"), 0.001);
        assertEquals(0.5f, parseFloat("one half"), 0.001);
        assertEquals(2.5f, parseFloat("two & one half"), 0.001);
        assertEquals(2.5f, parseFloat("two and one half"), 0.001);
    }

    @Test
    public void badThings() {
        assertNull(parseFloat(null));
        assertNull(parseFloat(""));
        assertNull(parseFloat("    "));
        assertNull(parseFloat("glergypants"));
        assertNull(parseFloat("1.2.3"));
        assertNull(parseFloat("1-3"));
    }

}