package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;

public interface PantryItemSearchRepository {

    long countTotalUses(PantryItem pantryItem);

    SearchResponse<PantryItem> search(PantryItemSearchRequest request);

}
