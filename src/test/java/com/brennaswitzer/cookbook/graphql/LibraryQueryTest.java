package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.persistence.NoResultException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LibraryQueryTest extends MockTest {

    @MockTestTarget
    private LibraryQuery query;

    @Mock
    private RecipeService recipeService;

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

}