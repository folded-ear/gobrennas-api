package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.PantryItem;

public record PantryItemNeedsDuplicatesFound(PantryItem item) {

    public Long id() {
        return item.getId();
    }

}
