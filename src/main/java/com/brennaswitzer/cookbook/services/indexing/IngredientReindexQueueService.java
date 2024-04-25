package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.async.QueueMonitor;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
public class IngredientReindexQueueService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private QueueMonitor queueMonitor;

    @Transactional
    public IndexStats getIndexStats() {
        IndexStats.IndexStatsBuilder builder = IndexStats.builder();
        builder.queueStats(queueMonitor.getStats("q_ingredient_fulltext"));
        builder.ingredientCount(countIngredients());
        builder.indexedIngredientCount(countIngredients(
                q -> q.append("WHERE fulltext IS NOT NULL\n")));
        builder.staleIngredientCount(countIngredients(
                q -> q.append("""
                              WHERE fulltext IS NOT NULL
                                AND EXISTS(SELECT *
                                           FROM q_ingredient_fulltext
                                           WHERE id = ingredient.id
                                  )
                              """)));
        return builder.build();
    }

    private long countIngredients() {
        return countIngredients(query -> {
            // everything!
        });
    }

    private long countIngredients(Consumer<NamedParameterQuery> whereAction) {
        NamedParameterQuery query = new NamedParameterQuery(
                """
                SELECT COUNT(*) count
                FROM ingredient
                """);
        whereAction.accept(query);
        SqlRowSet rs = jdbcTemplate.queryForRowSet(query.getStatement(),
                                                   query.getParameters());
        rs.next();
        return rs.getLong(1);
    }

    public void enqueueIngredient(Ingredient ingredient) {
        // This isn't _required_, but will avoid a double-reindex if the
        // transactions resolve in the right order. If they don't, the scheduled
        // reindexer will pick it up.
        enqueue(query -> query.append(
                "VALUES (:id)\n",
                "id",
                ingredient.getId()));
        eventPublisher.publishEvent(new ReindexIngredientEvent(ingredient.getId()));
    }

    public void enqueueRecipesWithIngredient(Ingredient ingredient) {
        enqueue(query -> query.append(
                """
                SELECT recipe_id
                FROM recipe_ingredients
                WHERE ingredient_id = :id
                """,
                "id",
                ingredient.getId()));
    }

    public void enqueueIngredientsWithLabel(Label label) {
        enqueue(query -> query.append(
                """
                SELECT ingredient.id
                FROM ingredient_labels link
                     JOIN ingredient ON ingredient.id = link.ingredient_id
                WHERE link.label_id = :id
                """,
                "id",
                label.getId()));
    }

    @SuppressWarnings("UnusedReturnValue")
    private int enqueue(Consumer<NamedParameterQuery> selectAction) {
        NamedParameterQuery query = new NamedParameterQuery(
                "INSERT INTO q_ingredient_fulltext (id)\n");
        selectAction.accept(query);
        query.append("ON CONFLICT DO NOTHING\n");
        return jdbcTemplate.update(query.getStatement(),
                                   query.getParameters());
    }

}
