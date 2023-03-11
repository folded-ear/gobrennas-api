package com.brennaswitzer.cookbook.repositories;

import org.springframework.data.domain.Sort;

public interface SearchRequest {

    int getOffset();

    int getLimit();

    Sort getSort();

    default boolean isOffset() {
        return getOffset() > 0;
    }

    @SuppressWarnings("unused")
    default boolean isSorted() {
        return getSort().isSorted();
    }

}
