package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.LabelService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LibraryMutationTest extends MockTest {

    @MockTestTarget
    private LibraryMutation mutation;

    @Mock
    private RecipeService recipeService;
    @Mock
    private ItemService itemService;
    @Mock
    private LabelService labelService;
    @Mock
    private EntityManager entityManager;

    @Test
    void createRecipe_client() {
        var recipe = mock(Recipe.class);
        List<String> labels = new ArrayList<>();
        var info = mock(IngredientInfo.class);
        when(info.getLabels()).thenReturn(labels);
        when(info.asRecipe(any())).thenReturn(recipe);
        var photo = mock(Part.class);
        when(recipeService.createNewRecipe(any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.createRecipe(info, photo, false);

        assertSame(recipe, result);
        verify(info).asRecipe(entityManager);
        verify(recipe, never()).getIngredients(); // don't re-recognize
        var captor = ArgumentCaptor.forClass(Upload.class);
        verify(recipeService).createNewRecipe(same(recipe), captor.capture());
        var upload = captor.getValue();
        verify(photo, never()).getContentType();
        upload.getContentType();
        verify(photo).getContentType();
        verify(labelService).updateLabels(same(recipe), same(labels));
    }

    @Test
    void createRecipe_cookThis() {
        var recipe = mock(Recipe.class);
        List<String> labels = new ArrayList<>();
        var info = mock(IngredientInfo.class);
        when(info.asRecipe(any())).thenReturn(recipe);
        when(info.getLabels()).thenReturn(labels);
        when(recipeService.createNewRecipe(any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.createRecipe(info, null, true);

        assertSame(recipe, result);
        verify(info).asRecipe(entityManager);
        verify(recipe).getIngredients(); // auto-recognize
        verify(recipeService).createNewRecipe(same(recipe), isNull());
        verify(labelService).updateLabels(same(recipe), same(labels));
    }

    @Test
    void createRecipeFrom() {
        var recipe = mock(Recipe.class);
        List<String> labels = new ArrayList<>();
        var info = mock(IngredientInfo.class);
        when(info.getLabels()).thenReturn(labels);
        when(info.asRecipe(any())).thenReturn(recipe);
        when(recipeService.createNewRecipeFrom(any(), any(), any()))
                .thenAnswer(iom -> iom.getArgument(1));

        var result = mutation.createRecipeFrom(123L, info, null);

        assertSame(recipe, result);
        verify(info).asRecipe(entityManager);
        verify(recipeService).createNewRecipeFrom(eq(123L), same(recipe), isNull());
        verify(labelService).updateLabels(same(recipe), same(labels));
    }

    @Test
    void updateRecipe() {
        var recipe = mock(Recipe.class);
        List<String> labels = new ArrayList<>();
        var info = mock(IngredientInfo.class);
        when(info.getLabels()).thenReturn(labels);
        when(info.asRecipe(any())).thenReturn(recipe);
        var photo = mock(Part.class);
        when(recipeService.updateRecipe(any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.updateRecipe(123L, info, photo);

        assertSame(recipe, result);
        verify(info).setId(123L);
        verify(recipeService).updateRecipe(same(recipe), notNull());
        verify(labelService).updateLabels(same(recipe), same(labels));
    }

    @Test
    void setRecipePhoto() {
        var photo = mock(Part.class);
        var recipe = mock(Recipe.class);
        when(recipeService.setRecipePhoto(any(), any()))
                .thenReturn(recipe);

        var result = mutation.setRecipePhoto(123L, photo);

        assertSame(recipe, result);
        verify(recipeService).setRecipePhoto(eq(123L), notNull());
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
