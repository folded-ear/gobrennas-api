package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.graphql.model.Section;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SectionResolver implements GraphQLResolver<Section> {

    public List<String> labels(Section section) {
        return section.getLabels()
                .stream()
                .map(Label::getName)
                .toList();
    }

}
