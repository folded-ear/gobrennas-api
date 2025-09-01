package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.loaders.PantryItemDuplicateCountBatchLoader;
import com.brennaswitzer.cookbook.graphql.loaders.PantryItemUseCountBatchLoader;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<Long> useCount(PantryItem pantryItem,
                                            DataFetchingEnvironment env) {
        return env.<PantryItem, Long>getDataLoader(PantryItemUseCountBatchLoader.class.getName())
                .load(pantryItem);
    }

    @SchemaMapping
    public CompletableFuture<Long> duplicateCount(PantryItem pantryItem,
                                                  DataFetchingEnvironment env) {
        return env.<PantryItem, Long>getDataLoader(PantryItemDuplicateCountBatchLoader.class.getName())
                .load(pantryItem);
    }

    @SchemaMapping
    public OffsetDateTime firstUse(PantryItem item) {
        return item.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
