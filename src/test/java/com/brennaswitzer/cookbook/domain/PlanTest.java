package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlanTest {

    @Test
    public void owner() {
        Plan plan = new Plan();
        assertNull(plan.getOwner());
        User u = new User();
        plan.setOwner(u);
        assertSame(u, plan.getOwner());
    }

    @Test
    public void assigneeIsOwner() {
        Plan plan = new Plan();
        assertNull(plan.getAssignee());
        User u = new User();
        plan.setOwner(u);
        assertSame(u, plan.getAssignee());
    }

    @Test
    public void assigneeCannotBeSet() {
        Plan plan = new Plan(new User(), "the plan");
        assertThrows(UnsupportedOperationException.class,
                     () -> plan.setAssignee(new User()));
    }

}
