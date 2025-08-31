package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.graphql.model.Section;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchType;
import com.brennaswitzer.cookbook.services.IngredientService;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.ShareHelper;
import graphql.relay.Connection;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class LibraryQuery {

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    public static class LibrarySearch implements GqlSearch {

        LibrarySearchScope scope;
        String query;
        Set<Long> ingredients;
        int first;
        OffsetConnectionCursor after;

        LibrarySearchRequest toRecipeRequest() {
            return buildRequest()
                    .type(LibrarySearchType.TOP_LEVEL_RECIPE)
                    .build();
        }

        LibrarySearchRequest toSectionRequest() {
            return buildRequest()
                    .type(LibrarySearchType.OWNED_SECTION)
                    .build();
        }

        private LibrarySearchRequest.LibrarySearchRequestBuilder buildRequest() {
            return LibrarySearchRequest.builder()
                    .scope(scope)
                    .filter(query)
                    .ingredientIds(ingredients)
                    .offset(getOffset())
                    .limit(first);
        }

    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    public static class SuggestionSearch implements GqlSearch {

        int first;
        OffsetConnectionCursor after;

    }

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private ShareHelper shareHelper;

    public Connection<Recipe> recipes(
            LibrarySearch search,
            LibrarySearchScope scope,
            String query,
            Set<Long> ingredients,
            int first,
            OffsetConnectionCursor after
    ) {
        if (search == null) {
            search = LibrarySearch.builder()
                    .scope(scope)
                    .query(query)
                    .ingredients(ingredients)
                    .first(first)
                    .after(after)
                    .build();
        }
        SearchResponse<Recipe> rs = recipeService.searchLibrary(search.toRecipeRequest());
        return new OffsetConnection<>(rs);
    }

    public Connection<Section> sections(LibrarySearch search) {
        SearchResponse<Section> rs = recipeService.searchLibrary(search.toSectionRequest())
                .map(Section::new);
        return new OffsetConnection<>(rs);
    }

    public Recipe getRecipeById(Long id,
                                String optionalSecret,
                                DataFetchingEnvironment env) {
        ensurePrincipalOrSecret(id, optionalSecret, env);
        return recipeService.findRecipeById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no recipe with id: " + id));
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
            SuggestionSearch search,
            int first,
            OffsetConnectionCursor after) {
        if (search == null) {
            search = SuggestionSearch.builder()
                    .first(first)
                    .after(after)
                    .build();
        }
        SearchResponse<Recipe> rs = recipeService.suggestRecipes(
                search.getOffset(),
                search.getFirst());
        return new OffsetConnection<>(rs);
    }

    public Collection<Ingredient> bulkIngredients(Collection<Long> ids) {
        return ingredientService.bulkIngredients(ids);
    }

    private void ensurePrincipalOrSecret(Long id,
                                         String optionalSecret,
                                         DataFetchingEnvironment env) {
        // this check is much faster than validating the secret
        if (PrincipalUtil.optionally(env).isPresent()) {
            return;
        }
        // anonymous, see if the secret is good
        if (optionalSecret != null
            && shareHelper.isSecretValid(Recipe.class,
                                         id,
                                         optionalSecret)) {
            return; // woo!
        }
        // at this point, guaranteed to throw
        PrincipalUtil.ensurePrincipal(env);
    }

}
