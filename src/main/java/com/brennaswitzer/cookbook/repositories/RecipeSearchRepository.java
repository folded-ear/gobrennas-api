package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RecipeSearchRepository {

    Slice<Recipe> searchRecipes(
            String term,
            Pageable pageable
    );

}
