package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.loaders.PantryItemUseCountBatchLoader;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class PantryItemResolver implements GraphQLResolver<PantryItem> {

    public Set<String> synonyms(PantryItem pantryItem) {
        // This should be a no-op, but it's enforced softly to avoid unneeded
        // data access. Since we're loading them anyway, clean it up for sure.
        pantryItem.removeSynonym(pantryItem.getName());
        return pantryItem.getSynonyms();
    }

    public Set<String> labels(PantryItem pantryItem) {
        return pantryItem.getLabels()
                .stream()
                .map(Label::getName)
                .collect(Collectors.toSet());
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
