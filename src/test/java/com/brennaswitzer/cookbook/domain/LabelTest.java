package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LabelTest {

    @Test
    public void createLabelTest() {
        Label label = new Label("chicken");
        assertEquals("chicken", label.getName());
    }
}
