package com.brennaswitzer.cookbook.services.indexing;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientReindexQueueServiceImplTest {

    @InjectMocks
    private IngredientReindexQueueServiceImpl service;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<Map<String, ?>> paramsCaptor;

    @Test
    void enqueueIngredient() {
        Long id = 12345L;
        Recipe recipe = mock(Recipe.class);
        when(recipe.getId())
            .thenReturn(id);

        service.enqueueIngredient(recipe);

        verify(jdbcTemplate)
            .update(sqlCaptor.capture(),
                    paramsCaptor.capture());
        assertEquals("""
                     INSERT INTO ingredient_fulltext_reindex_queue (id)
                     VALUES (:id)
                     ON CONFLICT DO NOTHING
                     """,
                     sqlCaptor.getValue());
        assertEquals(Map.of("id", id),
                     paramsCaptor.getValue());
        verify(eventPublisher)
                .publishEvent(argThat((ReindexIngredientEvent e) -> id.equals(e.ingredientId())));
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
        assertEquals("""
                     INSERT INTO ingredient_fulltext_reindex_queue (id)
                     SELECT recipe_id
                     FROM recipe_ingredients
                     WHERE ingredient_id = :id
                     ON CONFLICT DO NOTHING
                     """,
                     sqlCaptor.getValue());
        assertEquals(Map.of("id", id),
                     paramsCaptor.getValue());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void enqueueIngredientsWithLabel() {
        Long id = 12345L;
        Label label = mock(Label.class);
        when(label.getId())
            .thenReturn(id);

        service.enqueueIngredientsWithLabel(label);

        verify(jdbcTemplate)
            .update(sqlCaptor.capture(),
                    paramsCaptor.capture());
        assertEquals("""
                     INSERT INTO ingredient_fulltext_reindex_queue (id)
                     SELECT ingredient.id
                     FROM ingredient_labels link
                          JOIN ingredient ON ingredient.id = link.ingredient_id
                     WHERE link.label_id = :id
                     ON CONFLICT DO NOTHING
                     """,
                     sqlCaptor.getValue());
        assertEquals(Map.of("id", id),
                     paramsCaptor.getValue());
        verifyNoInteractions(eventPublisher);
    }

}
