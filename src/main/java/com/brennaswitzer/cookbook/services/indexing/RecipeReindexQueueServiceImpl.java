package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.function.Consumer;

@Service
@Profile("!test")
public class RecipeReindexQueueServiceImpl implements RecipeReindexQueueService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public IndexStats getIndexStats() {
        IndexStats.IndexStatsBuilder builder = IndexStats.builder();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(
                "SELECT COUNT(*)\n" +
                        "     , COALESCE(CAST(EXTRACT(EPOCH FROM NOW() - MIN(ts)) AS BIGINT), -1)\n" +
                        "     , COALESCE(CAST(EXTRACT(EPOCH FROM NOW() - MAX(ts)) AS BIGINT), -1)\n" +
                        "FROM recipe_fulltext_reindex_queue\n",
                Collections.emptyMap());
        rs.next();
        builder.queueSize(rs.getLong(1))
                .queueMaxAge(rs.getLong(2))
                .queueMinAge(rs.getLong(3));
        builder.recipeCount(countRecipes());
        builder.indexedRecipeCount(countRecipes(
                q -> q.append("  AND recipe_fulltext IS NOT NULL\n")));
        builder.staleRecipeCount(countRecipes(
                q -> q.append("  AND recipe_fulltext IS NOT NULL\n")
                        .append("  AND EXISTS(SELECT *\n" +
                                        "             FROM recipe_fulltext_reindex_queue\n" +
                                        "             WHERE id = ingredient.id\n" +
                                        "    )\n")));
        return builder.build();
    }

    private long countRecipes() {
        return countRecipes(query -> {
            // everything!
        });
    }

    private long countRecipes(Consumer<NamedParameterQuery> whereAction) {
        NamedParameterQuery query = new NamedParameterQuery(
                "SELECT COUNT(*) count\n" +
                        "FROM ingredient\n" +
                        "WHERE dtype = 'Recipe'\n");
        whereAction.accept(query);
        SqlRowSet rs = jdbcTemplate.queryForRowSet(query.getStatement(),
                                                   query.getParameters());
        rs.next();
        return rs.getLong(1);
    }

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
        NamedParameterQuery query = new NamedParameterQuery(
                "INSERT INTO recipe_fulltext_reindex_queue (id)\n");
        selectAction.accept(query);
        query.append("ON CONFLICT DO NOTHING\n");
        return jdbcTemplate.update(query.getStatement(),
                                   query.getParameters());
    }

}
