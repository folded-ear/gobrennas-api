package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    @Override
    Iterable<Recipe> findAll();

    Iterable<Recipe> findByOwner(User owner);

    @Override
    Optional<Recipe> findById(Long aLong);

    @Override
    void deleteById(Long aLong);
}
