package com.brennaswitzer.cookbook.domain.measure;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuantityTest {

    private UnitOfMeasure inch, foot, yard, gram;

    @Before
    public void setUp() {
        inch = new UnitOfMeasure("inch", "in", "inches");
        foot = new UnitOfMeasure("foot", "ft", "feet");
        yard = new UnitOfMeasure("yard", "yd", "yards");
        gram = new UnitOfMeasure("gram", "g", "grams");
        inch.addConversion(foot, 1f / 12f);
        yard.addConversion(foot, 3f);
    }

    @Test
    public void inchToFoot() {
        assertEquals(
                new Quantity(1f, foot),
                new Quantity(12f, inch).convertTo(foot)
        );
    }

    @Test
    public void footToInch() {
        assertEquals(
                new Quantity(12f, inch),
                new Quantity(1f, foot).convertTo(inch)
        );
    }

    @Test
    public void yardToFoot() {
        assertEquals(
                new Quantity(3f, foot),
                new Quantity(1f, yard).convertTo(foot)
        );
    }

    @Test
    public void footToYard() {
        assertEquals(
                new Quantity(1f, yard),
                new Quantity(3f, foot).convertTo(yard)
        );
    }

    @Test
    public void yardToInch() {
        assertEquals(
                new Quantity(36f, inch),
                new Quantity(1f, yard).convertTo(inch)
        );
    }

    @Test
    public void inchToYard() {
        assertEquals(
                new Quantity(1f, yard),
                new Quantity(36f, inch).convertTo(yard)
        );
    }

    @Test(expected = NoConversionException.class)
    public void noConversion() {
        new Quantity(1f, yard).convertTo(gram);
    }
}