package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import org.springframework.data.repository.CrudRepository;

public interface PantryItemRepository extends CrudRepository<PantryItem, Long> {

}
