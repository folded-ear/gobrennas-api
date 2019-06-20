package com.brennaswitzer.cookbook.domain;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccessLevelTest {

    @Test
    public void includes() {
        assertTrue(AccessLevel.ADMINISTER.includes(AccessLevel.VIEW));
        assertTrue(AccessLevel.ADMINISTER.includes(AccessLevel.ADMINISTER));
        assertFalse(AccessLevel.VIEW.includes(AccessLevel.ADMINISTER));
    }

}