package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Component
public class RecipeFulltextIndexer {

    public static final int BATCH_SIZE = 5;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate txTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reindexRecipeImmediately(ReindexRecipeEvent event) {
        NamedParameterQuery query = new NamedParameterQuery(
            "DELETE\n" +
                "FROM recipe_fulltext_reindex_queue\n" +
                "WHERE id = :id",
            "id",
            event.getRecipeId());
        int rowsAffected = jdbcTemplate.update(query.getStatement(),
                                               query.getParameters());
        if (rowsAffected == 0) {
            // The transactions didn't resolve in our favor, so force it. It'll
            // end up being reindexed (as a no-op) in the future. If you're not
            // first, you're last!
            query = new NamedParameterQuery(
                "select recipe_fulltext_update(:id)",
                "id",
                event.getRecipeId());
            jdbcTemplate.query(query.getStatement(),
                               query.getParameters(),
                               rs -> {
                               });
        }
    }

    @Scheduled(fixedDelay = 10, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void reindexQueued() {
        NamedParameterQuery query = new NamedParameterQuery(
            "DELETE\n" +
                "FROM recipe_fulltext_reindex_queue\n" +
                "WHERE id IN (SELECT id\n" +
                "             FROM recipe_fulltext_reindex_queue\n" +
                "             ORDER BY ts\n" +
                "             LIMIT :batch_size)",
            "batch_size",
            BATCH_SIZE);
        while (true) {
            @SuppressWarnings("DataFlowIssue") // box/unbox shenanigans
            int rowsAffected = txTemplate.execute(
                tx -> jdbcTemplate.update(query.getStatement(),
                                          query.getParameters()));
            if (rowsAffected < BATCH_SIZE) break;
        }
    }

}
