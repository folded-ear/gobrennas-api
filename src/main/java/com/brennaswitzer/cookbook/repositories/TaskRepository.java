package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends BaseEntityRepository<Task> {

    @Query("from Task where parent is null and acl.owner = ?1")
    Iterable<Task> findByOwnerAndParentIsNull(User owner);

    Iterable<Task> findByIngredient(Ingredient ing);

}
