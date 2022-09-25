package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface PantryItemRepository extends CrudRepository<PantryItem, Long> {

    List<PantryItem> findByNameIgnoreCaseOrderById(String name);

    List<PantryItem> findAllByNameIgnoreCaseContainingOrderById(String name);

    List<PantryItem> findAllByUpdatedAtIsAfter(Instant cutoff);

}
