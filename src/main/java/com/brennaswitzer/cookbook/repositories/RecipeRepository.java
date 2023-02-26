package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends BaseEntityRepository<Recipe>, RecipeSearchRepository {

    @Override
    List<Recipe> findAll();

    List<Recipe> findByOwnerAndNameIgnoreCaseOrderById(User owner, String name);

    List<Recipe> findAllByOwnerAndNameIgnoreCaseContainingOrderById(User owner, String name);

    @Override
    Optional<Recipe> findById(Long aLong);

    @Override
    void deleteById(Long aLong);

}
