package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class RecipeReindexQueueService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void enqueueRecipe(Recipe recipe) {
        // This isn't _required_, but  will avoid a double-reindex if the
        // transactions resolve in the right order. If they don't, the scheduled
        // reindexer will pick it up.
        enqueue(query -> query.append(
            "VALUES (:id)\n",
            "id",
            recipe.getId()));
        eventPublisher.publishEvent(new ReindexRecipeEvent(recipe.getId()));
    }

    public void enqueueRecipesWithIngredient(Ingredient ingredient) {
        enqueue(query -> query.append(
            "SELECT recipe_id\n" +
                "FROM recipe_ingredients\n" +
                "WHERE ingredient_id = :id\n",
            "id",
            ingredient.getId()));
    }

    public void enqueueRecipesWithLabel(Label label) {
        enqueue(query -> query.append(
            "SELECT recipe.id\n" +
                "FROM ingredient_labels link\n" +
                "     JOIN ingredient recipe ON recipe.id = link.ingredient_id\n" +
                "WHERE link.label_id = :id\n" +
                "  AND recipe.dtype = 'Recipe'\n",
            "id",
            label.getId()));
    }

    @SuppressWarnings("UnusedReturnValue")
    private int enqueue(Consumer<NamedParameterQuery> selectAction) {
        NamedParameterQuery query = new NamedParameterQuery();
        query.append("INSERT INTO recipe_fulltext_reindex_queue (id)\n");
        selectAction.accept(query);
        query.append("ON CONFLICT DO NOTHING\n");
        return jdbcTemplate.update(query.getStatement(),
                                   query.getParameters());
    }

}
