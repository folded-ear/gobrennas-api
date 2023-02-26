package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeFulltextServiceTest {

    @InjectMocks
    private RecipeFulltextService service;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<Map<String, ?>> paramsCaptor;

    @Test
    void enqueueRecipe() {
        Long id = 12345L;
        Recipe recipe = mock(Recipe.class);
        when(recipe.getId())
            .thenReturn(id);

        service.enqueueRecipe(recipe);

        verify(jdbcTemplate)
            .update(sqlCaptor.capture(),
                    paramsCaptor.capture());
        assertEquals("INSERT INTO recipe_fulltext_reindex_queue (id)\n" +
                         "VALUES (:id)\n" +
                         "ON CONFLICT DO NOTHING\n",
                     sqlCaptor.getValue());
        assertEquals(Map.of("id", id),
                     paramsCaptor.getValue());
    }

    @Test
    void enqueueRecipesWithIngredient() {
        Long id = 12345L;
        Ingredient ing = mock(PantryItem.class);
        when(ing.getId())
            .thenReturn(id);

        service.enqueueRecipesWithIngredient(ing);

        verify(jdbcTemplate)
            .update(sqlCaptor.capture(),
                    paramsCaptor.capture());
        assertEquals("INSERT INTO recipe_fulltext_reindex_queue (id)\n" +
                         "SELECT recipe_id\n" +
                         "FROM recipe_ingredients\n" +
                         "WHERE ingredient_id = :id\n" +
                         "ON CONFLICT DO NOTHING\n",
                     sqlCaptor.getValue());
        assertEquals(Map.of("id", id),
                     paramsCaptor.getValue());
    }

    @Test
    void enqueueRecipesWithLabel() {
        Long id = 12345L;
        Label label = mock(Label.class);
        when(label.getId())
            .thenReturn(id);

        service.enqueueRecipesWithLabel(label);

        verify(jdbcTemplate)
            .update(sqlCaptor.capture(),
                    paramsCaptor.capture());
        assertEquals("INSERT INTO recipe_fulltext_reindex_queue (id)\n" +
                         "SELECT recipe.id\n" +
                         "FROM ingredient_labels link\n" +
                         "     JOIN ingredient recipe ON recipe.id = link.ingredient_id\n" +
                         "WHERE link.label_id = :id\n" +
                         "  AND recipe.dtype = 'Recipe'\n" +
                         "ON CONFLICT DO NOTHING\n",
                     sqlCaptor.getValue());
        assertEquals(Map.of("id", id),
                     paramsCaptor.getValue());
    }

}
