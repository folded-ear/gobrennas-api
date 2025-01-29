package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import com.brennaswitzer.cookbook.util.NoUserPrincipalException;
import com.brennaswitzer.cookbook.util.ShareHelper;
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
    @Mock
    private ShareHelper shareHelper;

    @BeforeEach
    void setUp() {
        authenticated();
        when(env.getGraphQlContext()).thenReturn(gqlCtx);
    }

    @Test
    void getRecipeById_anonymous_noSecret() {
        anonymously();

        assertThrows(NoUserPrincipalException.class,
                     () -> query.getRecipeById(4L, null, env));

        verifyNoInteractions(recipeService);
    }

    @Test
    void getRecipeById_anonymous_wrongSecret() {
        anonymously();
        when(shareHelper.isSecretValid(any(), any(), any())).thenReturn(false);

        assertThrows(NoUserPrincipalException.class,
                     () -> query.getRecipeById(4L, "garbage", env));

        verifyNoInteractions(recipeService);
    }

    @Test
    void getRecipeById_anonymous_correctSecret() {
        anonymously();
        when(shareHelper.isSecretValid(Recipe.class, 4L, "secret"))
                .thenReturn(true);
        when(recipeService.findRecipeById(any()))
                .thenReturn(Optional.of(mock(Recipe.class)));

        query.getRecipeById(4L, "secret", env);

        verify(recipeService).findRecipeById(4L);
    }

    @Test
    void getRecipeById_unknown() {
        authenticated();
        when(recipeService.findRecipeById(any())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class,
                     () -> query.getRecipeById(4L, null, env));

        verify(recipeService).findRecipeById(4L);
    }

    @Test
    void getRecipeById() {
        authenticated();
        Recipe recipe = mock(Recipe.class);
        when(recipeService.findRecipeById(any())).thenReturn(Optional.of(recipe));
        assertSame(recipe, query.getRecipeById(4L, null, env));
    }

    @Test
    void recognizeItem_anonymous() {
        anonymously();
        assertThrows(NoUserPrincipalException.class,
                     () -> query.recognizeItem("goat", 14, env));
        verifyNoInteractions(itemService);
    }

    @Test
    void recognizeItem_cursor() {
        authenticated();
        query.recognizeItem("goat", 14, env);
        verify(itemService).recognizeItem("goat", 14, false);
    }

    @Test
    void recognizeItem_noCursor() {
        authenticated();
        var mock = mock(RecognizedItem.class);
        when(itemService.recognizeItem("goat", 4, false))
                .thenReturn(mock);

        var result = query.recognizeItem("goat", null, env);

        assertSame(mock, result);
    }

    private void authenticated() {
        when(gqlCtx.getOrEmpty(UserPrincipal.class))
                .thenReturn(Optional.of(mock(UserPrincipal.class)));
    }

    private void anonymously() {
        when(gqlCtx.getOrEmpty(UserPrincipal.class))
                .thenReturn(Optional.empty());
    }

}
