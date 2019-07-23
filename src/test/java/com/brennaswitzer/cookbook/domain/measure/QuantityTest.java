package com.brennaswitzer.cookbook.domain.measure;

import com.brennaswitzer.cookbook.domain.NoConversionException;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
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
        inch.addConversion(foot, 1.0 / 12);
        yard.addConversion(foot, 3);
    }

    @Test
    public void inchToFoot() {
        assertEquals(
                new Quantity(1, foot),
                new Quantity(12, inch).convertTo(foot)
        );
    }

    @Test
    public void footToInch() {
        assertEquals(
                new Quantity(12, inch),
                new Quantity(1, foot).convertTo(inch)
        );
    }

    @Test
    public void yardToFoot() {
        assertEquals(
                new Quantity(3, foot),
                new Quantity(1, yard).convertTo(foot)
        );
    }

    @Test
    public void footToYard() {
        assertEquals(
                new Quantity(1, yard),
                new Quantity(3, foot).convertTo(yard)
        );
    }

    @Test
    public void yardToInch() {
        assertEquals(
                new Quantity(36, inch),
                new Quantity(1, yard).convertTo(inch)
        );
    }

    @Test
    public void inchToYard() {
        assertEquals(
                new Quantity(1, yard),
                new Quantity(36, inch).convertTo(yard)
        );
    }

    @Test(expected = NoConversionException.class)
    public void noConversion() {
        new Quantity(1, yard).convertTo(gram);
    }
}