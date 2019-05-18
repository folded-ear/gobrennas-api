package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    @Override
    Iterable<Recipe> findAll();

    @Override
    Optional<Recipe> findById(Long aLong);

    @Override
    void deleteById(Long aLong);
}
