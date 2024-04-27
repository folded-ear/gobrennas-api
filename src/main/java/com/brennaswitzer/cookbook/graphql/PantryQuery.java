package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.SortDir;
import com.brennaswitzer.cookbook.services.PantryItemService;
import graphql.relay.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PantryQuery extends PagingQuery {

    private static final int DEFAULT_LIMIT = 25;
    private static final String DUPLICATE_PREFIX = "duplicates:";

    @Autowired
    private PantryItemService pantryItemService;

    public Connection<PantryItem> search(
            String query,
            String sortBy,
            SortDir sortDir,
            Integer first,
            OffsetConnectionCursor after
    ) {
        String[] rawTerms = query.split(" +");
        List<String> filterTerms = new ArrayList<>(rawTerms.length);
        Long duplicateOf = null;
        for (var t : rawTerms) {
            if (t.startsWith(DUPLICATE_PREFIX)) {
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
        SearchResponse<PantryItem> rs = pantryItemService.search(
                PantryItemSearchRequest.builder()
                        .filter(String.join(" ", filterTerms))
                        .duplicateOf(duplicateOf)
                        .sort(getSort(sortBy, sortDir))
                        .offset(getOffset(after))
                        .limit(getLimit(first))
                        .build());
        return new OffsetConnection<>(rs);
    }

    private Sort getSort(String sortBy, SortDir sortDir) {
        if (sortBy == null || sortBy.isBlank()) return null;
        Sort.Direction dir = SortDir.DESC == sortDir
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(dir, sortBy);
    }

    private int getLimit(Integer first) {
        return first == null || first <= 0 ? DEFAULT_LIMIT : first;
    }

}
