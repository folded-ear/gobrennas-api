package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collection;

public interface RecipeSearchRepository {

    Slice<Recipe> searchRecipes(User user,
                                String term,
                                Pageable pageable
    );

    Slice<Recipe> searchRecipesByOwner(User user,
                                       Collection<User> owners,
                                       String term,
                                       Pageable pageable
    );

}
