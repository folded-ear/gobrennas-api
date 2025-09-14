package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
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
    @Order(EventHandlerSlot.REINDEX)
    @Synchronized
    public void reindexIngredientImmediately(IngredientNeedsReindexing event) {
        NamedParameterQuery query = new NamedParameterQuery(
                """
                DELETE
                FROM q_ingredient_fulltext
                WHERE id = :id
                """,
                "id",
                event.id());
        int rowsAffected = jdbcTemplate.update(query.getStatement(),
                                               query.getParameters());
        if (rowsAffected == 0) {
            // The transactions didn't resolve in our favor, so force it. It'll
            // end up being reindexed (as a no-op) in the future. If you're not
            // first, you're last!
            query = new NamedParameterQuery(
                    "select q_ingredient_fulltext_handler(:id)",
                    "id",
                    event.id());
            jdbcTemplate.query(query.getStatement(),
                               query.getParameters(),
                               rs -> {
                               });
            log.info("reindexed ingredient '{}', leaving it queued", event.id());
        }
        log.info("reindexed ingredient '{}' immediately", event.id());
    }

}
