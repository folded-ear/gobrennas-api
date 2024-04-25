package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.payload.QueueStats;
import com.brennaswitzer.cookbook.services.async.QueueMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/_queue")
@PreAuthorize("hasRole('DEVELOPER')")
public class QueueController {

    @Autowired
    private QueueMonitor monitor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTmpl;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Map<String, Object>> getQueues() {
        return jdbcTmpl.queryForList(
                        """
                        SELECT table_name AS queue_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name LIKE 'q\\_%' ESCAPE '\\'
                        ORDER BY 1
                        """,
                        Collections.emptyMap())
                .stream()
                .peek(it -> it.put("stats",
                                   monitor.getStats((String) it.get("queue_name"))))
                .collect(Collectors.toList());
    }

    @GetMapping("/{queue}")
    @ResponseStatus(HttpStatus.OK)
    public QueueStats getStats(@PathVariable String queue) {
        return monitor.getStats(queue);
    }

}
