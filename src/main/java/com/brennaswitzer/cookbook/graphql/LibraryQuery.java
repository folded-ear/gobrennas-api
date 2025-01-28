package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import graphql.relay.Connection;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LibraryQuery extends PagingQuery {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ItemService itemService;

    public Connection<Recipe> recipes(
            LibrarySearchScope scope,
            String query,
            Set<Long> ingredientIds,
            int first,
            OffsetConnectionCursor after
    ) {
        SearchResponse<Recipe> rs = recipeService.searchRecipes(scope, query, ingredientIds, getOffset(after), first);
        return new OffsetConnection<>(rs);
    }

    public Recipe getRecipeById(Long id,
                                DataFetchingEnvironment env) {
        PrincipalUtil.ensurePrincipal(env);
        return recipeService.findRecipeById(id)
                .orElseThrow(() -> new NoResultException("There is no recipe with id: " + id));
    }

    public RecognizedItem recognizeItem(String raw,
                                        Integer cursor,
                                        DataFetchingEnvironment env) {
        PrincipalUtil.ensurePrincipal(env);
        // never request suggestions, so the resolver can process 'count'
        return itemService.recognizeItem(
                raw,
                cursor == null ? raw.length() : cursor,
                false);
    }

    public Connection<Recipe> suggestRecipesToCook(
            int first,
            OffsetConnectionCursor after) {
        SearchResponse<Recipe> rs = recipeService.suggestRecipes(getOffset(after), first);
        return new OffsetConnection<>(rs);
    }

}
