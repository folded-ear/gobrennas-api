package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PlanItemRepository extends BaseEntityRepository<PlanItem> {

    int countByStatusNot(PlanItemStatus status);

    Iterable<PlanItem> findByIngredient(Ingredient ing);

    @Modifying
    @Query("delete from PlanItem where trashBin is not null and updatedAt < ?1")
    int deleteByUpdatedAtBeforeAndTrashBinIsNotNull(Instant cutoff);

    @Query(value = """
                     WITH RECURSIVE items AS (SELECT id, updated_at
                                                FROM plan_item
                                               WHERE id = :rootId
                                               UNION
                                              SELECT plan_item.id
                                                   , plan_item.updated_at
                                                FROM plan_item
                                                   , items
                                               WHERE plan_item.parent_id = items.id
                                                  OR plan_item.aggregate_id = items.id
                                                  OR plan_item.trash_bin_id = items.id)
                   SELECT id
                     FROM items
                    WHERE updated_at > :cutoff
                   """,
            nativeQuery = true)
    List<Long> getUpdatedSince(Long rootId, Instant cutoff);
}
