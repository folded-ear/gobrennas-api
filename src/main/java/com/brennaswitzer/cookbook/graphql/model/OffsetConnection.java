package com.brennaswitzer.cookbook.graphql.model;

import com.brennaswitzer.cookbook.repositories.SearchResponse;
import graphql.relay.Connection;
import graphql.relay.DefaultEdge;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OffsetConnection<T> implements Connection<T> {

    private final List<Edge<T>> edges;
    private final PageInfo pageInfo;

    public OffsetConnection(SearchResponse<T> rs) {
        int offset = rs.getOffset();
        edges = new ArrayList<>(rs.size());
        int i = 0;
        for (T r : rs.getContent()) {
            edges.add(new DefaultEdge<>(r, new OffsetConnectionCursor(offset + i++)));
        }
        pageInfo = new DefaultPageInfo(
                rs.isEmpty() ? null : new OffsetConnectionCursor(offset),
                rs.isEmpty() ? null : new OffsetConnectionCursor(offset + rs.size() - 1),
                rs.hasPrevious(),
                rs.hasNext());
    }

}
