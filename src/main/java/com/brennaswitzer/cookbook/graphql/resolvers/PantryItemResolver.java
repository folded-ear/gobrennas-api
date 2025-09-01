package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PantryItemResolver {

    @SchemaMapping
    public List<String> synonyms(PantryItem pantryItem) {
        // This should be a no-op, but it's enforced softly to avoid unneeded
        // data access. Since we're loading them anyway, clean it up for sure.
        pantryItem.removeSynonym(pantryItem.getName());
        List<String> syns = new ArrayList<>(pantryItem.getSynonyms());
        syns.sort(String::compareToIgnoreCase);
        return syns;
    }

    @SchemaMapping
    public List<String> labels(PantryItem pantryItem) {
        return pantryItem.getLabels()
                .stream()
                .map(Label::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    @SchemaMapping
    public OffsetDateTime firstUse(PantryItem item) {
        return item.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
