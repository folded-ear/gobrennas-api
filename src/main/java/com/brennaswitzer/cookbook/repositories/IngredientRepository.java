package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import org.springframework.data.repository.history.RevisionRepository;

public interface IngredientRepository extends BaseEntityRepository<Ingredient>, RevisionRepository<Ingredient, Long, Long> {

}
