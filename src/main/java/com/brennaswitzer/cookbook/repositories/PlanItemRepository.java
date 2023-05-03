package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PlanItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface PlanItemRepository extends BaseEntityRepository<PlanItem> {

    Iterable<PlanItem> findByIngredient(Ingredient ing);

    @Modifying
    @Query("delete from PlanItem where trashBin is not null and updatedAt < ?1")
    int deleteByUpdatedAtBeforeAndTrashBinIsNotNull(Instant cutoff);

}
