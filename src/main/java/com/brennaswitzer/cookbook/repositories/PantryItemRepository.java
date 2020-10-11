package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PantryItemRepository extends CrudRepository<PantryItem, Long> {

    List<PantryItem> findByNameIgnoreCaseOrderById(String name);

}
