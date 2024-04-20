package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ReindexIngredients {

    public static final int BATCH_SIZE = 5;

    @Autowired
    private TransactionTemplate txTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public int enqueueAll() {
        var q = new NamedParameterQuery(
                """
                INSERT INTO ingredient_fulltext_reindex_queue
                SELECT id
                FROM ingredient
                ON CONFLICT DO NOTHING
                """);
        int rows = jdbcTemplate.update(q.getStatement(),
                                       q.getParameters());
        log.info("Enqueued {} ingredients for reindexing", rows);
        return rows;
    }

    @Scheduled(fixedDelay = 10, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Synchronized
    @SuppressWarnings("ScheduledMethodInspection")
    public int reindexQueued() {
        int totalRows = 0;
        var q = new NamedParameterQuery(
                """
                DELETE
                FROM ingredient_fulltext_reindex_queue
                WHERE id IN (SELECT id
                             FROM ingredient_fulltext_reindex_queue
                             ORDER BY ts
                             LIMIT :batch_size)
                """,
                "batch_size",
                BATCH_SIZE);
        int rowsAffected;
        do {
            //noinspection DataFlowIssue
            rowsAffected = txTemplate.execute(
                    tx -> jdbcTemplate.update(q.getStatement(),
                                              q.getParameters()));
            totalRows += rowsAffected;
        } while (rowsAffected == BATCH_SIZE);
        log.info("Reindexed {} ingredients", totalRows);
        return totalRows;
    }

}
