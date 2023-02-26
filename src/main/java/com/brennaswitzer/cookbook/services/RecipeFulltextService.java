package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class RecipeFulltextService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public int enqueueRecipe(Recipe recipe) {
        return enqueue(query -> query.append(
            "VALUES (:id)\n",
            "id",
            recipe.getId()));
    }

    public int enqueueRecipesWithIngredient(Ingredient ingredient) {
        return enqueue(query -> query.append(
            "SELECT recipe_id\n" +
                "FROM recipe_ingredients\n" +
                "WHERE ingredient_id = :id\n",
            "id",
            ingredient.getId()));
    }

    public int enqueueRecipesWithLabel(Label label) {
        return enqueue(query -> query.append(
            "SELECT recipe.id\n" +
                "FROM ingredient_labels link\n" +
                "     JOIN ingredient recipe ON recipe.id = link.ingredient_id\n" +
                "WHERE link.label_id = :id\n" +
                "  AND recipe.dtype = 'Recipe'\n",
            "id",
            label.getId()));
    }

    private int enqueue(Consumer<NamedParameterQuery> selectAction) {
        NamedParameterQuery query = new NamedParameterQuery();
        query.append("INSERT INTO recipe_fulltext_reindex_queue (id)\n");
        selectAction.accept(query);
        query.append("ON CONFLICT DO NOTHING\n");
        return jdbcTemplate.update(query.getStatement(),
                                   query.getParameters());
    }

}
