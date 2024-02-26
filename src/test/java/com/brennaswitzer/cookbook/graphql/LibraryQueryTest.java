package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LibraryQueryTest extends MockTest {

    @MockTestTarget
    private LibraryQuery query;

    @Mock
    private RecipeService recipeService;
    @Mock
    private ItemService itemService;

    @Test
    void testNoRecipeById() {
        when(recipeService.findRecipeById(any())).thenReturn(Optional.empty());
        assertThrows(NoResultException.class, () -> query.getRecipeById(4L));
    }

    @Test
    void testGetRecipeById() {
        Recipe recipe = mock(Recipe.class);
        when(recipeService.findRecipeById(any())).thenReturn(Optional.of(recipe));
        assertSame(recipe, query.getRecipeById(4L));
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
