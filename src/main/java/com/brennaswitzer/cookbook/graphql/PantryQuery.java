package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.PantryItem_;
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

    @Autowired
    private PantryItemService pantryItemService;

    public Connection<PantryItem> search(
            String query,
            String sortBy,
            SortDir sortDir,
            int first,
            OffsetConnectionCursor after
    ) {
        Sort.Direction dir = SortDir.DESC.equals(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = sortBy == null || sortBy.isBlank()
                ? Sort.by(dir, PantryItem_.NAME, PantryItem_.ID)
                : Sort.by(dir, sortBy);
        SearchResponse<PantryItem> rs = pantryItemService.search(query, sort, getOffset(after), first);
        return new OffsetConnection<>(rs);
    }

}
