package com.brennaswitzer.cookbook.repositories.impl;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.SearchRequest;
import com.brennaswitzer.cookbook.util.ValueUtils;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Sort;

import java.util.Set;

@Value
@Builder(toBuilder = true)
public class LibrarySearchRequest implements SearchRequest {

    @Builder.Default
    LibrarySearchScope scope = LibrarySearchScope.MINE;
    @Builder.Default
    LibrarySearchType type = LibrarySearchType.TOP_LEVEL_RECIPE;
    User user;
    String filter;
    Set<Long> ingredientIds;
    int offset;
    @Builder.Default
    int limit = 25;
    Sort sort;

    public boolean isFiltered() {
        return ValueUtils.hasValue(filter);
    }

    public boolean isOwnerConstrained() {
        return !LibrarySearchScope.EVERYONE.equals(scope);
    }

    public boolean isIngredientConstrained() {
        return ValueUtils.hasValue(ingredientIds);
    }

}
