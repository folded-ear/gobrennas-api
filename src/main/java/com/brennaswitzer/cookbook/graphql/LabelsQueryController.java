package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class LabelsQueryController {

    record LabelsQuery() {}

    @Autowired
    private LabelService labelService;

    @QueryMapping
    LabelsQuery labels() {
        return new LabelsQuery();
    }

    @SchemaMapping
    Iterable<Label> all(LabelsQuery lblsQ) {
        return labelService.findAllLabels();
    }

}
