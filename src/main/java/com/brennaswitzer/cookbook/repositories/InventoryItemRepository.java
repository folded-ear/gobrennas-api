package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface InventoryItemRepository extends BaseEntityRepository<InventoryItem> {

    Slice<InventoryItem> findByUser(User user, Pageable pageable);

}
