package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    @Override
    Iterable<Recipe> findAll();

    @Override
    Iterable<Recipe> findAllById(Iterable<Long> iterable);
}
