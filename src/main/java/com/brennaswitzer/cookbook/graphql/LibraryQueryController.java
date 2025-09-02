package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.graphql.model.Section;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchType;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.IngredientService;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.NoUserPrincipalException;
import com.brennaswitzer.cookbook.util.ShareHelper;
import graphql.relay.Connection;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.Set;

@Controller
public class LibraryQueryController {

    record LibraryQuery() {}

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    static class LibrarySearch implements GqlSearch {

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
    static class SuggestionSearch implements GqlSearch {

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

    @QueryMapping
    LibraryQuery library() {
        return new LibraryQuery();
    }

    @SchemaMapping
    Connection<Recipe> recipes(
            LibraryQuery libQ,
            @Argument LibrarySearch search,
            @Argument LibrarySearchScope scope,
            @Argument String query,
            @Argument Set<Long> ingredients,
            @Argument int first,
            @Argument OffsetConnectionCursor after
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

    @SchemaMapping
    Connection<Section> sections(LibraryQuery libQ,
                                 @Argument LibrarySearch search) {
        SearchResponse<Section> rs = recipeService.searchLibrary(search.toSectionRequest())
                .map(Section::new);
        return new OffsetConnection<>(rs);
    }

    @SchemaMapping
    Recipe getRecipeById(LibraryQuery libQ,
                         @Argument Long id,
                         @Argument("secret") String optionalSecret,
                         @CurrentUser UserPrincipal userPrincipal) {
        ensurePrincipalOrSecret(id, optionalSecret, userPrincipal);
        return recipeService.findRecipeById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no recipe with id: " + id));
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    RecognizedItem recognizeItem(LibraryQuery libQ,
                                 @Argument String raw,
                                 @Argument Integer cursor) {
        // never request suggestions, so the resolver can process 'count'
        return itemService.recognizeItem(
                raw,
                cursor == null ? raw.length() : cursor,
                false);
    }

    @SchemaMapping
    Connection<Recipe> suggestRecipesToCook(
            LibraryQuery libQ,
            @Argument SuggestionSearch search,
            @Argument int first,
            @Argument OffsetConnectionCursor after) {
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

    @SchemaMapping
    Collection<Ingredient> bulkIngredients(LibraryQuery libQ,
                                           @Argument Collection<Long> ids) {
        return ingredientService.bulkIngredients(ids);
    }

    private void ensurePrincipalOrSecret(Long id,
                                         String optionalSecret,
                                         UserPrincipal userPrincipal) {
        // If they're authenticated, we're good
        if (userPrincipal != null) {
            return;
        }
        // anonymous, see if the secret is good
        if (optionalSecret != null
            && shareHelper.isSecretValid(Recipe.class,
                                         id,
                                         optionalSecret)) {
            return; // woo!
        }
        // nope!
        throw new NoUserPrincipalException();
    }

}
