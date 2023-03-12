package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LabelsQuery {

    @Autowired
    private LabelService labelService;

    public Iterable<Label> all() { return labelService.findAllLabels(); }
}
