package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Task;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface TaskRepository extends BaseEntityRepository<Task> {

    Iterable<Task> findByIngredient(Ingredient ing);

    @Modifying
    @Query("delete from Task where trashBin is not null and updatedAt < ?1")
    int deleteByUpdatedAtBeforeAndTrashBinIsNotNull(Instant cutoff);

}
