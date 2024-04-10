package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;

import java.util.Collection;
import java.util.Map;

public interface PantryItemSearchRepository {

    Map<PantryItem, Long> countTotalUses(Collection<PantryItem> items);

    SearchResponse<PantryItem> search(PantryItemSearchRequest request);

}
