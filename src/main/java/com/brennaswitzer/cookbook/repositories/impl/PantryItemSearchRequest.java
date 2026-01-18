package com.brennaswitzer.cookbook.repositories.impl;

import com.brennaswitzer.cookbook.domain.PantryItem_;
import com.brennaswitzer.cookbook.repositories.SearchRequest;
import com.brennaswitzer.cookbook.util.ValueUtils;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Sort;

@Value
@Builder
public class PantryItemSearchRequest implements SearchRequest {

    String filter;
    Long duplicateOf;
    int offset;
    int limit;
    Sort sort;

    public boolean isFiltered() {
        return ValueUtils.hasValue(filter);
    }

    public boolean isDuplicateOf() {
        return duplicateOf != null;
    }

    public int getLimit() {
        if (limit <= 0) return 10;
        return limit;
    }

    public Sort getSort() {
        if (sort == null) {
            return Sort.by(PantryItem_.NAME);
        }
        return sort;
    }

}
