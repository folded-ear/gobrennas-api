package com.brennaswitzer.cookbook.domain;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PantryItemTest {

    @Test
    void synonyms() {
        val it = new PantryItem("the curd-y stuff");

        assertFalse(it.hasSynonym(null));
        assertFalse(it.hasSynonym("garbage"));
        assertTrue(it.addSynonym("cheese"));
        assertFalse(it.addSynonym("cheese"));
        it.addSynonyms("Cheez", "CHEEZE");
        assertEquals(Set.of("cheese", "Cheez", "CHEEZE"),
                     it.getSynonyms());
        assertSame(it, it.withSynonym("cheddar"));
        assertSame(it, it.withSynonyms("BRIE", "Stilton", "garbage"));
        assertTrue(it.removeSynonym("garbage"));
        assertFalse(it.removeSynonym("garbage"));
        assertFalse(it.hasSynonym("garbage"));
        assertTrue(it.hasSynonym("cheese")); // exact
        assertTrue(it.hasSynonym("cheez")); // ignore case
        assertTrue(it.hasSynonym("chEEZ")); // ignore case
        assertTrue(it.hasSynonym("stilton")); // ignore case
    }

}
