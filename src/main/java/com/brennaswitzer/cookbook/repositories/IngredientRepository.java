package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import org.springframework.data.jpa.repository.Query;

public interface IngredientRepository extends BaseEntityRepository<Ingredient> {

    // The @Query is used to get case-insensitive ordering, which Spring Data
    // doesn't support via method name modifiers (only predicates).
    // todo: my dear god. index this.
    @Query("from Ingredient\n" +
            "where lower(name) like %:lcSubstr%\n" +
            "order by lower(name)\n" +
            "    , id")
    Iterable<Ingredient> findByNameContainsIgnoreCaseOrderByNameIgnoreCaseAscIdAsc(String lcSubstr);

}
