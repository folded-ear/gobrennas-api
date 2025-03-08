package com.brennaswitzer.cookbook.services.async;

import com.brennaswitzer.cookbook.payload.QueueStats;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QueueMonitor {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTmpl;

    public QueueStats getStats(String queue) {
        var builder = QueueStats.builder();
        var q = new NamedParameterQuery("SELECT COUNT(*)\n")
                .append("     , COALESCE(CAST(EXTRACT(EPOCH FROM NOW() - MIN(ts)) AS BIGINT), -1)\n")
                .append("     , COALESCE(CAST(EXTRACT(EPOCH FROM NOW() - MAX(ts)) AS BIGINT), -1)\n")
                .append("FROM ").identifier(queue);
        SqlRowSet rs = jdbcTmpl.queryForRowSet(q.getStatement(),
                                               q.getParameters());
        rs.next();
        builder.size(rs.getLong(1))
                .maxAge(rs.getLong(2))
                .minAge(rs.getLong(3));
        return builder.build();
    }

}
