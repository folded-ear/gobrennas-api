package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    @Override
    Iterable<Recipe> findAll();

    @Transactional
    Recipe findRecipeById(Long Id);
}
