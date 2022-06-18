package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientTest {

    Ingredient i;

    @BeforeEach
    public void setUp() throws Exception {
        i = new Ingredient() {};
    }

    @Test
    public void emptyLabelsTest() {
        assertTrue(i.getLabels().isEmpty());
    }

    @Test
    public void addLabelRefTest() {
        Label label = new Label("chicken");
        i.addLabel(label);
        assertTrue(i.getLabels().contains(label));
    }

    @Test
    public void removeLabelRefTest() {
        Label label = new Label("chicken");
        Label label2 = new Label("chicken");
        i.addLabel(label);
        assertTrue(i.getLabels().contains(label2));
        i.removeLabel(label2);
        assertTrue(i.getLabels().isEmpty());
    }

    @Test
    public void getLabelsTest() {
        List<Label> labels = new ArrayList<>();
        labels.add(new Label("chicken"));
        labels.add(new Label("dinner"));
        labels.add(new Label("make-ahead"));

        i.addLabels(labels);
        assertEquals(i.getLabels().size(), 3);
    }

    @Test
    public void clearLabelsTest() {
        i.addLabel(new Label("chicken"));
        i.addLabel(new Label("dinner"));
        i.addLabel(new Label("make-ahead"));
        assertEquals(3, i.getLabels().size());
        i.clearLabels();
        assertEquals(0, i.getLabels().size());
    }

}
