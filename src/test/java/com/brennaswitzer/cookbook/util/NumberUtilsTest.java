package com.brennaswitzer.cookbook.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.util.Locale;

import static com.brennaswitzer.cookbook.util.NumberUtils.parseNumber;
import static com.brennaswitzer.cookbook.util.NumberUtils.parseNumberWithRange;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NumberUtilsTest {

    @Test
    @ResourceLock(Resources.LOCALE)
    public void formatRawNumberIgnoresLocaleAndGrouping() {
        Locale original = Locale.getDefault(Locale.Category.FORMAT);
        try {
            for (Locale locale : new Locale[]{Locale.US, Locale.GERMANY, Locale.FRANCE}) {
                Locale.setDefault(Locale.Category.FORMAT, locale);
                assertEquals("2000", NumberUtils.formatRawNumber(2000));
                assertEquals("2000.125", NumberUtils.formatRawNumber(2000.125));
                assertEquals(2000.125, parseNumber(NumberUtils.formatRawNumber(2000.125)));
                assertEquals("0.00001", NumberUtils.formatRawNumber(0.00001));
                assertEquals("100000000000000000000", NumberUtils.formatRawNumber(1e20));
            }
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original);
        }
    }

    @Test
    public void formatRawNumberRejectsNonFiniteQuantities() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> NumberUtils.formatRawNumber(value));
        }
    }

    @Test
    public void parseDecimal() {
        assertEquals(0, parseNumber("0"), 0.001);
        assertEquals(1, parseNumber("1"), 0.001);
        assertEquals(1, parseNumber("1.0"), 0.001);
        assertEquals(0.1, parseNumber("0.1"), 0.001);
        assertEquals(0.01, parseNumber("0.01"), 0.001);
        assertEquals(1.23, parseNumber("1.23"), 0.001);
        assertEquals(-1, parseNumber("-1"), 0.001);
        assertEquals(-1.23, parseNumber("-1.23"), 0.001);
        assertEquals(-0.01, parseNumber("-0.01"), 0.001);
    }

    @Test
    public void parseFraction() {
        assertEquals(0.5, parseNumber("1/2"), 0.001);
        assertEquals(0.5, parseNumber("1/ 2"), 0.001);
        assertEquals(0.5, parseNumber("1 /2"), 0.001);
        assertEquals(0.5, parseNumber("1 / 2"), 0.001);
        assertEquals(1.5, parseNumber("3 / 2"), 0.001);
        assertEquals(-1.5, parseNumber("-3 / 2"), 0.001);

        assertEquals(0.333, parseNumber("1/3"), 0.001);
        assertEquals(0.667, parseNumber("2/3"), 0.001);

        // U+2044 : FRACTION SLASH {solidus (in typography)}
        assertEquals(0.5, parseNumber("1⁄2"), 0.001);
    }

    @Test
    public void parseProperFraction() {
        assertEquals(3.5, parseNumber("3&1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 & 1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 and 1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 ½"), 0.001);
        assertEquals(3.5, parseNumber("3½"), 0.001);
        assertEquals(3.5, parseNumber("3&½"), 0.001);
        assertEquals(3.5, parseNumber("3 & ½"), 0.001);
        assertEquals(3.5, parseNumber("3 and ½"), 0.001);
        assertEquals(-3.5, parseNumber("-3 & 1/2"), 0.001);
        assertEquals(-3.5, parseNumber("-3 and ½"), 0.001);
    }

    @Test
    public void parseName() {
        assertEquals(0.5, parseNumber("half"), 0.001);
        assertEquals(0.5, parseNumber("one half"), 0.001);
        assertEquals(2.5, parseNumber("two & one half"), 0.001);
        assertEquals(2.5, parseNumber("two and one half"), 0.001);
        assertEquals(0.0, parseNumber("zero"), 0.001);
        assertEquals(0.5, parseNumber("zero & one half"), 0.001);
    }

    @Test
    public void parseBadThings() {
        assertNull(parseNumber(null));
        assertNull(parseNumber(""));
        assertNull(parseNumber("    "));
        assertNull(parseNumber("glergypants"));
        assertNull(parseNumber("1-3"));
        assertNull(parseNumber("1.2.3"));
        assertNull(parseNumber("01"));
        //noinspection ConstantConditions
        assertEquals(1.2, parseNumber("1.2.3", true), 0.001);
    }

    @Test
    public void parseMixedThings() {
        assertEquals(3.5, parseNumber("three and 1/2"), 0.001);
        assertEquals(3.5, parseNumber("three & 1/2"), 0.001);
        assertEquals(3.5, parseNumber("one and 1 1/2 and ½ & 0.5"), 0.001);
    }

    @Test
    public void parseWithCoords() {
        NumberUtils.NumberWithRange nwr = parseNumberWithRange("  1 and 2/3 cups fish");
        System.out.println(nwr);
        assertEquals(5.0 / 3.0, nwr.number(), 0.001);
        assertEquals(2, nwr.start());
        assertEquals(11, nwr.end());
    }

    @Test
    public void parseNegativeWithCoords() {
        NumberUtils.NumberWithRange nwr = parseNumberWithRange(" - two and a half tsp water");
        System.out.println(nwr);
        assertEquals(-2.5, nwr.number(), 0.001);
        assertEquals(1, nwr.start());
        assertEquals(17, nwr.end());
    }

}
