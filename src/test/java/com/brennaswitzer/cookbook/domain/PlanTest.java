package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class PlanTest {

    @Test
    public void owner() {
        Plan list = new Plan();
        assertNull(list.getOwner());
        User u = new User();
        list.setOwner(u);
        assertSame(u, list.getOwner());
    }

}
