package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;

public interface PantryItemSearchRepository {

    SearchResponse<PantryItem> search(PantryItemSearchRequest request);

}
