package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.repositories.SearchResponse;
import graphql.relay.Connection;
import graphql.relay.DefaultEdge;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class OffsetConnection<T> implements Connection<T> {

    @Getter
    private final List<Edge<T>> edges;
    @Getter
    private final PageInfo pageInfo;

    public OffsetConnection(SearchResponse<T> rs) {
        int offset = rs.getOffset();
        edges = new ArrayList<>(rs.getSize());
        int i = 0;
        for (T r : rs.getContent()) {
            edges.add(new DefaultEdge<>(r, new OffsetConnectionCursor(offset + i++)));
        }
        pageInfo = new DefaultPageInfo(
                new OffsetConnectionCursor(offset),
                new OffsetConnectionCursor(offset + rs.getSize() - 1),
                rs.hasPrevious(),
                rs.hasNext());
    }

}
