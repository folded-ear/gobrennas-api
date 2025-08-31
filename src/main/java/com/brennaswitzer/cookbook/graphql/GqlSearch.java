package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;

public interface GqlSearch {

    int getFirst();

    OffsetConnectionCursor getAfter();

    default int getOffset() {
        OffsetConnectionCursor after = getAfter();
        return after == null
                ? 0
                : after.getOffset() + 1;
    }

}
