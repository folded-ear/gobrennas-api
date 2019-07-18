package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Override
    List<Recipe> findAll();

    List<Recipe> findByOwner(User owner);

    Optional<Recipe> findOneByOwnerAndNameIgnoreCase(User owner, String name);

    @Override
    Optional<Recipe> findById(Long aLong);

    @Override
    void deleteById(Long aLong);
}
