package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class IngredientFulltextIndexer {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Synchronized
    public void reindexIngredientImmediately(ReindexIngredientEvent event) {
        NamedParameterQuery query = new NamedParameterQuery(
                """
                DELETE
                FROM ingredient_fulltext_reindex_queue
                WHERE id = :id
                """,
                "id",
                event.ingredientId());
        int rowsAffected = jdbcTemplate.update(query.getStatement(),
                                               query.getParameters());
        if (rowsAffected == 0) {
            // The transactions didn't resolve in our favor, so force it. It'll
            // end up being reindexed (as a no-op) in the future. If you're not
            // first, you're last!
            query = new NamedParameterQuery(
                    "select ingredient_fulltext_update(:id)",
                    "id",
                    event.ingredientId());
            jdbcTemplate.query(query.getStatement(),
                               query.getParameters(),
                               rs -> {
                               });
            log.info("couldn't reindex ingredient {}, queued instead", event.ingredientId());
        } else {
            log.info("reindexed ingredient {} immediately", event.ingredientId());
        }
    }

}
