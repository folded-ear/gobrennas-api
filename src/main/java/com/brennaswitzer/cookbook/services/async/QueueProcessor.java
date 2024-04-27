package com.brennaswitzer.cookbook.services.async;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class QueueProcessor {

    @Autowired
    private TransactionTemplate txTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final Lock lock = new ReentrantLock();
    private final String tableName;
    private final int batchSize;

    protected QueueProcessor(String tableName, int batchSize) {
        this.tableName = tableName;
        this.batchSize = batchSize;
    }

    @Transactional
    public int enqueueAll() {
        var q = new NamedParameterQuery()
                .append("INSERT INTO ").identifier(tableName).append("\n")
                .append(selectAllIds()).append("\n")
                .append("ON CONFLICT DO NOTHING\n");
        int rows = jdbcTemplate.update(q.getStatement(),
                                       q.getParameters());
        log.info("[{}] enqueued {} items", tableName, rows);
        return rows;
    }

    protected abstract NamedParameterQuery selectAllIds();

    public int drainQueue() {
        if (!lock.tryLock()) return 0;
        try {
            return drainQueueInternal();
        } finally {
            lock.unlock();
        }
    }

    private int drainQueueInternal() {
        int totalRows = 0;
        var watch = new StopWatch();
        watch.start();
        var q = new NamedParameterQuery("DELETE\n")
                .append("FROM ").identifier(tableName).append("\n")
                .append("WHERE id IN (SELECT id\n")
                .append("             FROM ").identifier(tableName).append("\n")
                .append("             ORDER BY ts\n")
                .append("             LIMIT ").bind(batchSize).append(")\n");
        int rowsAffected;
        do {
            //noinspection DataFlowIssue
            rowsAffected = txTemplate.execute(
                    tx -> jdbcTemplate.update(q.getStatement(),
                                              q.getParameters()));
            totalRows += rowsAffected;
        } while (rowsAffected == batchSize);
        watch.stop();
        log.info("[{}] drained {} items in {} ms", tableName, totalRows, watch.getTotalTimeMillis());
        return totalRows;
    }

}
