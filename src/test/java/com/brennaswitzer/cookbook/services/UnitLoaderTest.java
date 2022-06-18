package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
