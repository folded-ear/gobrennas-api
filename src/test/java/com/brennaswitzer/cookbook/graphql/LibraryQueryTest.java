package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import com.brennaswitzer.cookbook.util.NoUserPrincipalException;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LibraryQueryTest extends MockTest {

    @MockTestTarget
    private LibraryQuery query;

    @Mock
    private RecipeService recipeService;
    @Mock
    private ItemService itemService;
    @Mock
    private DataFetchingEnvironment env;
    @Mock
    private GraphQLContext gqlCtx;

    @BeforeEach
    void setUp() {
        when(gqlCtx.getOrEmpty(UserPrincipal.class))
                .thenReturn(Optional.of(mock(UserPrincipal.class)));
        when(env.getGraphQlContext()).thenReturn(gqlCtx);
    }

    @Test
    void getRecipeById_anonymous() {
        when(gqlCtx.getOrEmpty(UserPrincipal.class))
                .thenReturn(Optional.empty());
        assertThrows(NoUserPrincipalException.class,
                     () -> query.getRecipeById(4L, env));
        verifyNoInteractions(recipeService);
    }

    @Test
    void getRecipeById_unknown() {
        when(recipeService.findRecipeById(any())).thenReturn(Optional.empty());
        assertThrows(NoResultException.class, () -> query.getRecipeById(4L, env));
    }

    @Test
    void getRecipeById() {
        Recipe recipe = mock(Recipe.class);
        when(recipeService.findRecipeById(any())).thenReturn(Optional.of(recipe));
        assertSame(recipe, query.getRecipeById(4L, env));
    }

    @Test
    void recognizeItem_anonymous() {
        when(gqlCtx.getOrEmpty(UserPrincipal.class))
                .thenReturn(Optional.empty());
        assertThrows(NoUserPrincipalException.class,
                     () -> query.recognizeItem("goat", 14, env));
        verifyNoInteractions(itemService);
    }

    @Test
    void recognizeItem_cursor() {
        query.recognizeItem("goat", 14, env);
        verify(itemService).recognizeItem("goat", 14, false);
    }

    @Test
    void recognizeItem_noCursor() {
        var mock = mock(RecognizedItem.class);
        when(itemService.recognizeItem("goat", 4, false))
                .thenReturn(mock);

        var result = query.recognizeItem("goat", null, env);

        assertSame(mock, result);
    }

}
