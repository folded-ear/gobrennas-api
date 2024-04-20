package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.SortDir;
import com.brennaswitzer.cookbook.services.PantryItemService;
import graphql.relay.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PantryQuery extends PagingQuery {

    private static final int DEFAULT_LIMIT = 25;

    @Autowired
    private PantryItemService pantryItemService;

    public Connection<PantryItem> search(
            String query,
            String sortBy,
            SortDir sortDir,
            Integer first,
            OffsetConnectionCursor after
    ) {
        SearchResponse<PantryItem> rs = pantryItemService.search(
                query,
                getSort(sortBy, sortDir),
                getOffset(after),
                getLimit(first));
        return new OffsetConnection<>(rs);
    }

    public Connection<PantryItem> duplicates(
            Long itemId,
            String sortBy,
            SortDir sortDir,
            Integer first,
            OffsetConnectionCursor after
    ) {
        SearchResponse<PantryItem> rs = pantryItemService.duplicatesOf(
                itemId,
                getSort(sortBy, sortDir),
                getOffset(after),
                getLimit(first));
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
