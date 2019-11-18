package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // todo: my dear god. index this.
    @Query("from Ingredient\n" +
            "where lower(name) like %:lcSubstr%\n" +
            "order by lower(name)\n" +
            "    , id")
    Iterable<Ingredient> findByNameContains(String lcSubstr);

}
