package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.graphql.model.Section;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SectionResolver {

    @SchemaMapping
    public List<String> labels(Section section) {
        return section.getLabels()
                .stream()
                .map(Label::getName)
                .toList();
    }

}
