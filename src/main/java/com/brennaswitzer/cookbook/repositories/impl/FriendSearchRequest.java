package com.brennaswitzer.cookbook.repositories.impl;

import com.brennaswitzer.cookbook.repositories.SearchRequest;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.regex.Pattern;

@Value
@Builder
public class FriendSearchRequest implements SearchRequest {

    private static final int DEFAULT_LIMIT = 10;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    Long excludedUserId;
    String query;
    int offset;
    int limit;

    /**
     * I return my query's whitespace-separated terms, all of which a friend
     * must match. An empty list matches every friend.
     */
    public List<String> getTerms() {
        if (query == null) return List.of();
        return WHITESPACE.splitAsStream(query)
                .filter(t -> !t.isEmpty())
                .toList();
    }

    public int getLimit() {
        if (limit <= 0) return DEFAULT_LIMIT;
        return limit;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

}
