package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.support.Info2Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.RecipeService;
import graphql.schema.DataFetchingEnvironment;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryMutationTest {

    @InjectMocks
    private LibraryMutation mutation;

    @Mock
    private RecipeService recipeService;
    @Mock
    private Info2Recipe info2Recipe;
    @Mock
    private DataFetchingEnvironment env;
    @Mock
    private UserPrincipal userPrincipal;

    @Test
    void createRecipe_cookThis() {
        var recipe = mock(Recipe.class);
        var info = mock(IngredientInfo.class);
        when(info2Recipe.convert(userPrincipal,
                                 info,
                                 true))
                .thenReturn(recipe);
        when(recipeService.createNewRecipe(any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.createRecipe(info, true, userPrincipal);

        assertSame(recipe, result);
        verify(recipeService).createNewRecipe(same(recipe), same(info));
        verify(info).setId(null);
    }

    @Test
    void createRecipeFrom() {
        var recipe = mock(Recipe.class);
        var info = mock(IngredientInfo.class);
        when(info.getId()).thenReturn(null);
        when(info2Recipe.convert(userPrincipal,
                                 info))
                .thenReturn(recipe);
        when(recipeService.createNewRecipeFrom(any(), any(), any()))
                .thenAnswer(iom -> iom.getArgument(1));

        var result = mutation.createRecipeFrom(123L, info, userPrincipal);

        assertSame(recipe, result);
        verify(recipeService).createNewRecipeFrom(eq(123L), same(recipe), same(info));
    }

    @Test
    void createRecipeFrom_idMismatch() {
        var info = mock(IngredientInfo.class);
        when(info.getId()).thenReturn(456L);

        assertThrows(IllegalArgumentException.class,
                     () -> mutation.createRecipeFrom(123L, info, userPrincipal));
        verifyNoInteractions(info2Recipe);
        verifyNoInteractions(recipeService);
    }

    @Test
    void updateRecipe() {
        var recipe = mock(Recipe.class);
        var info = mock(IngredientInfo.class);
        when(info.getId()).thenReturn(null);
        when(info2Recipe.convert(userPrincipal,
                                 info))
                .thenReturn(recipe);
        var photo = mock(Part.class);
        when(recipeService.updateRecipe(any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.updateRecipe(123L, info, userPrincipal);

        assertSame(recipe, result);
        verify(info).setId(123L);
        verify(recipeService).updateRecipe(same(recipe), same(info));
    }

    @Test
    void updateRecipe_idMismatch() {
        var info = mock(IngredientInfo.class);
        when(info.getId()).thenReturn(456L);

        assertThrows(IllegalArgumentException.class,
                     () -> mutation.updateRecipe(123L, info, userPrincipal));
        verifyNoInteractions(info2Recipe);
        verifyNoInteractions(recipeService);
    }

    @Test
    void deleteRecipe() {
        when(recipeService.deleteRecipeById(any()))
                .thenAnswer(iom -> {
                    long id = iom.getArgument(0);
                    var r = mock(Recipe.class);
                    when(r.getId()).thenReturn(id);
                    when(r.getName()).thenReturn("Recipe " + id);
                    return r;
                });

        var d = mutation.deleteRecipe(123L);

        assertEquals(Long.valueOf(123L), d.getId());
        assertEquals("Recipe 123", d.getName());
    }

}
