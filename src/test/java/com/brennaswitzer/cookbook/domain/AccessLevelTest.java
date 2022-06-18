package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessLevelTest {

    @Test
    public void includes() {
        assertTrue(AccessLevel.ADMINISTER.includes(AccessLevel.VIEW));
        assertTrue(AccessLevel.ADMINISTER.includes(AccessLevel.ADMINISTER));
        assertFalse(AccessLevel.VIEW.includes(AccessLevel.ADMINISTER));
    }

}
