package com.brennaswitzer.cookbook.domain;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
