package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collection;

public interface RecipeSearchRepository {

    Slice<Recipe> searchRecipes(
            String term,
            Pageable pageable
    );

    Slice<Recipe> searchRecipesByOwner(
            Collection<User> owners,
            String term,
            Pageable pageable
    );

}
