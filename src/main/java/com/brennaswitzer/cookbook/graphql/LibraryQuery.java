package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import graphql.relay.Connection;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LibraryQuery {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ItemService itemService;

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

    @SuppressWarnings("unused")
    public PantryItem pantryItem() {
        throw new UnsupportedOperationException("library.pantryItem is not supported.");
    }

    public Recipe getRecipeById(Long id) {
        return recipeService.findRecipeById(id)
                .orElseThrow(() -> new NoResultException("There is no recipe with id: " + id));
    }

    public RecognizedItem recognizeItem(String raw, Integer cursor) {
        // never request suggestions, so the resolver can process 'count'
        return itemService.recognizeItem(
                raw,
                cursor == null ? raw.length() : cursor,
                false);
    }

}
