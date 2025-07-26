package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.graphql.support.Info2Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.RecipeService;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    @Mock
    private GraphQLContext graphQLContext;

    @BeforeEach
    void setUp() {
        // lenient, since delete and set photo still use UserPrincipalAccess
        lenient().when(graphQLContext.getOrEmpty(UserPrincipal.class))
                .thenReturn(Optional.of(userPrincipal));
        lenient().when(env.getGraphQlContext())
                .thenReturn(graphQLContext);
    }

    @Test
    void createRecipe_client() {
        var recipe = mock(Recipe.class);
        var info = mock(IngredientInfo.class);
        when(info2Recipe.convert(userPrincipal,
                                 info,
                                 false))
                .thenReturn(recipe);
        var photo = mock(Part.class);
        when(recipeService.createNewRecipe(any(), any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.createRecipe(info, false, photo, env);

        assertSame(recipe, result);
        var captor = ArgumentCaptor.forClass(Upload.class);
        verify(recipeService).createNewRecipe(same(recipe), same(info), captor.capture());
        var upload = captor.getValue();
        verify(photo, never()).getContentType();
        upload.getContentType();
        verify(photo).getContentType();
    }

    @Test
    void createRecipe_cookThis() {
        var recipe = mock(Recipe.class);
        var info = mock(IngredientInfo.class);
        when(info2Recipe.convert(userPrincipal,
                                 info,
                                 true))
                .thenReturn(recipe);
        when(recipeService.createNewRecipe(any(), any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.createRecipe(info, true, null, env);

        assertSame(recipe, result);
        verify(recipeService).createNewRecipe(same(recipe), same(info), isNull());
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
        when(recipeService.createNewRecipeFrom(any(), any(), any(), any()))
                .thenAnswer(iom -> iom.getArgument(1));

        var result = mutation.createRecipeFrom(123L, info, null, env);

        assertSame(recipe, result);
        verify(recipeService).createNewRecipeFrom(eq(123L), same(recipe), same(info), isNull());
    }

    @Test
    void createRecipeFrom_idMismatch() {
        var info = mock(IngredientInfo.class);
        when(info.getId()).thenReturn(456L);

        assertThrows(IllegalArgumentException.class,
                     () -> mutation.createRecipeFrom(123L, info, null, env));
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
        when(recipeService.updateRecipe(any(), any(), any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = mutation.updateRecipe(123L, info, photo, env);

        assertSame(recipe, result);
        verify(info).setId(123L);
        verify(recipeService).updateRecipe(same(recipe), same(info), notNull());
    }

    @Test
    void updateRecipe_idMismatch() {
        var info = mock(IngredientInfo.class);
        when(info.getId()).thenReturn(456L);

        assertThrows(IllegalArgumentException.class,
                     () -> mutation.updateRecipe(123L, info, null, env));
        verifyNoInteractions(info2Recipe);
        verifyNoInteractions(recipeService);
    }

    @Test
    void setRecipePhoto() {
        var photo = mock(Part.class);
        var recipe = mock(Recipe.class);
        when(recipeService.setRecipePhoto(any(), any(), any(), any()))
                .thenReturn(recipe);

        var result = mutation.setRecipePhoto(123L, null, null, photo);

        assertSame(recipe, result);
        verify(recipeService).setRecipePhoto(eq(123L), isNull(), isNull(), notNull());
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
