package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.services.PantryItemService;
import graphql.relay.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PantryQuery extends PagingQuery {

    @Autowired
    private PantryItemService pantryItemService;

    public Connection<PantryItem> search(
            String query,
            List<String> sortBy,
            int first,
            OffsetConnectionCursor after
    ) {
        SearchResponse<PantryItem> rs = pantryItemService.search(query, sortBy, getOffset(after), first);
        return new OffsetConnection<>(rs);
    }

}
