package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;

abstract class PagingQuery {

    protected int getOffset(OffsetConnectionCursor after) {
        return after == null
                ? 0
                : after.getOffset() + 1;
    }

}
