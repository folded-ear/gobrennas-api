package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TaskListTest {

    @Test
    public void owner() {
        TaskList list = new TaskList();
        assertNull(list.getOwner());
        User u = new User();
        list.setOwner(u);
        assertSame(u, list.getOwner());
    }

}
