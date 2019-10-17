package com.brennaswitzer.cookbook.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class LabelTest {

    @Test
    public void createLabelTest() {
        Label label = new Label("chicken");
        assertEquals("chicken", label.getName());
    }
}
