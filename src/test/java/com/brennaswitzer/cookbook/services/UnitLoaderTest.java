package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.measure.UnitOfMeasure;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnitLoaderTest {

    @Test
    public void loadEmIn() {
        Collection<UnitOfMeasure> units = new UnitLoader()
                .loadUnits("units.yml");
        assertFalse(units.isEmpty());
        assertTrue(units.stream().anyMatch(uom ->
                "gram".equals(uom.getName())));
    }

}