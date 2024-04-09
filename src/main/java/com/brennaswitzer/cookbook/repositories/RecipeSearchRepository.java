package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;

public interface RecipeSearchRepository {

    SearchResponse<Recipe> searchRecipes(LibrarySearchRequest request);

}
