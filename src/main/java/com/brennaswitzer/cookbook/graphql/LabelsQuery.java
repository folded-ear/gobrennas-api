package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class LabelsQuery {

    @Autowired
    private LabelService labelService;

    @SchemaMapping(typeName = "LabelsQuery")
    public Iterable<Label> all() { return labelService.findAllLabels(); }
}
