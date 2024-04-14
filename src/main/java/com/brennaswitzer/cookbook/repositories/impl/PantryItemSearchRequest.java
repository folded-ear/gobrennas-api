package com.brennaswitzer.cookbook.repositories.impl;

import com.brennaswitzer.cookbook.repositories.SearchRequest;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Sort;

@Value
@Builder
public class PantryItemSearchRequest implements SearchRequest {

    String filter;
    int offset;
    int limit;
    Sort sort;

    public boolean isFiltered() {
        return filter != null && !filter.isBlank();
    }
}
