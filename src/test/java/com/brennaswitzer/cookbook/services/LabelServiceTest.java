package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;

@WithAliceBobEve(authentication = false)
public class LabelServiceTest {

    @Autowired
    LabelService labelService;

    @Test
    public void getLabelsEmpty() {
        Iterable<Label> labels = labelService.findAllLabels();
        assertFalse(labels.iterator().hasNext());
    }
}
