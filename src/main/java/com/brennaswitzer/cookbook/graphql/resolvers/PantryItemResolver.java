package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.loaders.PantryItemUseCountBatchLoader;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class PantryItemResolver implements GraphQLResolver<PantryItem> {

    public List<String> synonyms(PantryItem pantryItem) {
        // This should be a no-op, but it's enforced softly to avoid unneeded
        // data access. Since we're loading them anyway, clean it up for sure.
        pantryItem.removeSynonym(pantryItem.getName());
        List<String> syns = new ArrayList<>(pantryItem.getSynonyms());
        syns.sort(String::compareToIgnoreCase);
        return syns;
    }

    public List<String> labels(PantryItem pantryItem) {
        return pantryItem.getLabels()
                .stream()
                .map(Label::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    public CompletableFuture<Long> useCount(PantryItem pantryItem,
                                            DataFetchingEnvironment env) {
        return env.<PantryItem, Long>getDataLoader(PantryItemUseCountBatchLoader.class.getName())
                .load(pantryItem);
    }

    public OffsetDateTime firstUse(PantryItem item) {
        return item.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
