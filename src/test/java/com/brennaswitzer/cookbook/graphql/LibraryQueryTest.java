package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.NoUserPrincipalException;
import com.brennaswitzer.cookbook.util.ShareHelper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryQueryTest {

    @InjectMocks
    private LibraryQuery query;

    @Mock
    private RecipeService recipeService;
    @Mock
    private ItemService itemService;
    @Mock
    private ShareHelper shareHelper;
    @Mock
    private UserPrincipal userPrincipal;

    @Test
    void getRecipeById_anonymous_noSecret() {
        assertThrows(NoUserPrincipalException.class,
                     () -> query.getRecipeById(4L, null, null));

        verifyNoInteractions(recipeService);
    }

    @Test
    void getRecipeById_anonymous_wrongSecret() {
        when(shareHelper.isSecretValid(any(), any(), any())).thenReturn(false);

        assertThrows(NoUserPrincipalException.class,
                     () -> query.getRecipeById(4L, "garbage", null));

        verifyNoInteractions(recipeService);
    }

    @Test
    void getRecipeById_anonymous_correctSecret() {
        when(shareHelper.isSecretValid(Recipe.class, 4L, "secret"))
                .thenReturn(true);
        when(recipeService.findRecipeById(any()))
                .thenReturn(Optional.of(mock(Recipe.class)));

        query.getRecipeById(4L, "secret", null);

        verify(recipeService).findRecipeById(4L);
    }

    @Test
    void getRecipeById_unknown() {
        when(recipeService.findRecipeById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                     () -> query.getRecipeById(4L, null, userPrincipal));

        verify(recipeService).findRecipeById(4L);
    }

    @Test
    void getRecipeById() {
        Recipe recipe = mock(Recipe.class);
        when(recipeService.findRecipeById(any())).thenReturn(Optional.of(recipe));
        assertSame(recipe, query.getRecipeById(4L, null, userPrincipal));
    }

    @Test
    void recognizeItem_cursor() {
        query.recognizeItem("goat", 14);
        verify(itemService).recognizeItem("goat", 14, false);
    }

    @Test
    void recognizeItem_noCursor() {
        var mock = mock(RecognizedItem.class);
        when(itemService.recognizeItem("goat", 4, false))
                .thenReturn(mock);

        var result = query.recognizeItem("goat", null);

        assertSame(mock, result);
    }

}
