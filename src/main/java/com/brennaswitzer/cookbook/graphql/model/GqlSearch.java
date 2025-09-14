package com.brennaswitzer.cookbook.graphql.model;

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
