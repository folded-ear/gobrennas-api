package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class PlanTest {

    @Test
    public void owner() {
        Plan plan = new Plan();
        assertNull(plan.getOwner());
        User u = new User();
        plan.setOwner(u);
        assertSame(u, plan.getOwner());
    }

}
