package com.brennaswitzer.cookbook.repositories.impl;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.SearchRequest;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Sort;

import java.util.Set;

@Value
@Builder
public class LibrarySearchRequest implements SearchRequest {

    LibrarySearchScope scope;
    User user;
    String filter;
    Set<Long> ingredientIds;
    int offset;
    int limit;
    Sort sort;

    public boolean isFiltered() {
        return filter != null && !filter.isBlank();
    }

    public boolean isOwnerConstrained() {
        return !LibrarySearchScope.EVERYONE.equals(scope);
    }

    public boolean isIngredientConstrained() {
        return ingredientIds != null && !ingredientIds.isEmpty();
    }

}
