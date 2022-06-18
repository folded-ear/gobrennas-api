package com.brennaswitzer.cookbook.domain.measure;

import com.brennaswitzer.cookbook.domain.NoConversionException;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuantityTest {

    private UnitOfMeasure inch, foot, yard, gram;

    @BeforeEach
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

    @Test
    public void noConversion() {
        Quantity q = new Quantity(1, yard);
        assertThrows(NoConversionException.class, () ->
                q.convertTo(gram));
    }

    @Test
    public void plus() {
        assertEquals(
                new Quantity(1, yard),
                new Quantity(0.5, yard)
                        .plus(new Quantity(0.5, yard))
        );
    }

    @Test
    public void shortCircuitPlus() {
        assertSame(
                Quantity.ONE,
                Quantity.ONE.plus(Quantity.ZERO)
        );
        assertSame(
                Quantity.ONE,
                Quantity.ZERO.plus(Quantity.ONE)
        );
    }

    @Test
    public void minus() {
        assertEquals(
                new Quantity(0.5, yard),
                new Quantity(1, yard)
                        .minus(new Quantity(0.5, yard))
        );
    }

    @Test
    public void unitAndQuantityComparator() {
        val tsp = new UnitOfMeasure("tsp");
        val gal = new UnitOfMeasure("gal");
        val comp = new Quantity.ByUnitAndQuantityComparator();
        // null unit before non-null
        assertTrue(comp.compare(Quantity.count(1), new Quantity(5, gal)) < 0);
        assertTrue(comp.compare(Quantity.count(5), new Quantity(1, gal)) < 0);
        // alpha by unit
        assertTrue(comp.compare(new Quantity(1, gal), new Quantity(5, tsp)) < 0);
        assertTrue(comp.compare(new Quantity(5, gal), new Quantity(1, tsp)) < 0);
        // break null unit ties w/ quantity
        assertTrue(comp.compare(new Quantity(1, tsp), new Quantity(5, tsp)) < 0);
        // break non-null unit ties w/ quantity
        assertTrue(comp.compare(Quantity.count(1), Quantity.count(5)) < 0);
    }

}
