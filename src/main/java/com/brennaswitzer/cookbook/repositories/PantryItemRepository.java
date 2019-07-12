package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PantryItemRepository extends CrudRepository<PantryItem, Long> {

    Optional<PantryItem> findOneByNameIgnoreCase(String name);

}
