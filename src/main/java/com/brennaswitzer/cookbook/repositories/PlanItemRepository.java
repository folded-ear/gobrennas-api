package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;

import java.time.Instant;

public interface PlanItemRepository extends BaseEntityRepository<PlanItem>, RevisionRepository<PlanItem, Long, Long> {

    int countByStatusNot(PlanItemStatus status);

    Iterable<PlanItem> findByIngredient(Ingredient ing);

    @Modifying
    @Query("delete from PlanItem where trashBin is not null and updatedAt < ?1")
    int deleteByUpdatedAtBeforeAndTrashBinIsNotNull(Instant cutoff);

}
