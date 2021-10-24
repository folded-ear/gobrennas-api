package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface InventoryItemRepository extends BaseEntityRepository<InventoryItem> {

    Slice<InventoryItem> findByUser(User user, Pageable pageable);

    Optional<InventoryItem> findByUserAndIngredient(User user, Ingredient ingredient);

}
