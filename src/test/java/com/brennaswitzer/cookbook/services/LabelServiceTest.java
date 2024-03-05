package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Label;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
public class LabelServiceTest {

    @Autowired
    LabelService labelService;

    @Test
    public void getLabelsEmpty() {
        Iterable<Label> labels = labelService.findAllLabels();
        assertFalse(labels.iterator().hasNext());
    }
}
