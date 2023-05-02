package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.services.RecipeService;
import graphql.relay.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@SuppressWarnings("unused") // reflected by java-graphql
@Component
public class LibraryQuery {

    @Autowired
    private RecipeService recipeService;

    public Connection<Recipe> recipes(
            LibrarySearchScope scope,
            String query,
            int first,
            OffsetConnectionCursor after
    ) {
        final int offset = after == null
                ? 0
                : after.getOffset() + 1;
        SearchResponse<Recipe> rs = recipeService.searchRecipes(scope, query, offset, first);
        return new OffsetConnection<>(rs);
    }

    public PantryItem pantryItem() {
        throw new UnsupportedOperationException("library.pantryItem is not supported.");
    }

    public Optional<Recipe> getRecipeById(Long id) {
        return recipeService.findRecipeById(id);
    }

}
