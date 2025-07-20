package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.graphql.model.Section;
import com.brennaswitzer.cookbook.mapper.LabelMapper;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SectionResolver implements GraphQLResolver<Section> {

    @Autowired
    private LabelMapper labelMapper;

    public List<String> labels(Section section) {
        return section.getLabels()
                .stream()
                .map(labelMapper::labelToString)
                .toList();
    }

}
