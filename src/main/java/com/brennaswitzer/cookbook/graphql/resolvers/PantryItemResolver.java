package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PantryItemResolver implements GraphQLResolver<PantryItem> {

    @Autowired
    private PantryItemRepository pantryItemRepository;

    public Set<String> synonyms(PantryItem pantryItem) {
        Set<String> all = pantryItem.getSynonyms();
        // This should be a no-op, but it's enforced softly to avoid unneeded
        // data access. Since we're loading them anyway, clean it up for sure.
        pantryItem.removeSynonym(pantryItem.getName());
        return all;
    }

    public Set<String> labels(PantryItem pantryItem) {
        return pantryItem.getLabels()
                .stream()
                .map(Label::getName)
                .collect(Collectors.toSet());
    }

    public long useCount(PantryItem pantryItem) {
        return pantryItemRepository.countTotalUses(pantryItem);
    }

    public OffsetDateTime firstUse(PantryItem item) {
        return item.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
