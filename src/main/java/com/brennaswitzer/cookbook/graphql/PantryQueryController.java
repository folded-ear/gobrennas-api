package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.GqlSearch;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.SortDir;
import com.brennaswitzer.cookbook.services.IngredientService;
import com.brennaswitzer.cookbook.services.PantryItemService;
import graphql.relay.Connection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class PantryQueryController {

    private static final int DEFAULT_LIMIT = 25;
    private static final String DUPLICATE_PREFIX = "duplicates:";

    record PantryQuery() {}

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    static class PantrySearch implements GqlSearch {

        String query;
        String sortBy;
        SortDir sortDir;
        int first;
        OffsetConnectionCursor after;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        transient List<String> filterTerms;
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        transient Long duplicateOf;

        List<String> getFilterTerms() {
            processQuery();
            return filterTerms;
        }

        Long getDuplicateOf() {
            processQuery();
            return duplicateOf;
        }

        Sort getSort() {
            if (sortBy == null || sortBy.isBlank()) return null;
            Sort.Direction dir = SortDir.DESC == sortDir
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            return Sort.by(dir, sortBy);
        }

        private void processQuery() {
            if (filterTerms != null) return;
            String[] rawTerms = query.split(" +");
            filterTerms = new ArrayList<>(rawTerms.length);
            for (var t : rawTerms) {
                if (t.startsWith(DUPLICATE_PREFIX)) {
                    if (duplicateOf != null) {
                        throw new IllegalArgumentException(String.format(
                                "Only one '%s' term is allowed, but '%s' has more",
                                DUPLICATE_PREFIX,
                                query));
                    }
                    String id = t.substring(DUPLICATE_PREFIX.length());
                    try {
                        duplicateOf = Long.valueOf(id);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException(String.format(
                                "Cannot parse '%s' as an item ID.",
                                id));
                    }
                } else {
                    filterTerms.add(t);
                }
            }
        }

    }

    @Autowired
    private PantryItemService pantryItemService;

    @Autowired
    private IngredientService ingredientService;

    @QueryMapping
    PantryQuery pantry() {
        return new PantryQuery();
    }

    @SchemaMapping
    Connection<PantryItem> pantryItems(PantryQuery pantryQ,
                                       @Argument PantrySearch search) {
        SearchResponse<PantryItem> rs = pantryItemService.search(
                PantryItemSearchRequest.builder()
                        .filter(String.join(" ", search.getFilterTerms()))
                        .duplicateOf(search.getDuplicateOf())
                        .sort(search.getSort())
                        .offset(search.getOffset())
                        .limit(search.getFirst())
                        .build());
        return new OffsetConnection<>(rs);
    }

    @SchemaMapping
    Connection<PantryItem> search(
            PantryQuery pantryQ,
            @Argument String query,
            @Argument String sortBy,
            @Argument SortDir sortDir,
            @Argument Integer first,
            @Argument OffsetConnectionCursor after
    ) {
        return pantryItems(pantryQ,
                           PantrySearch.builder()
                                   .query(query)
                                   .sortBy(sortBy)
                                   .sortDir(sortDir)
                                   .first(first)
                                   .after(after)
                                   .build());
    }

    @SchemaMapping
    Collection<Ingredient> bulkIngredients(PantryQuery pantryQ,
                                           @Argument Collection<Long> ids) {
        return ingredientService.bulkIngredients(ids);
    }

    @SchemaMapping
    Iterable<PantryItem> updatedSince(PantryQuery pantryQ,
                                      @Argument Long cutoff) {
        return pantryItemService.findAllByUpdatedAtIsAfter(Instant.ofEpochMilli(cutoff));
    }

}
