package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({ "SpringJavaAutowiredFieldsWarningInspection", "SqlResolve" })
@RestController
@RequestMapping("api/_db")
@PreAuthorize("hasRole('DEVELOPER')")
public class DbController {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTmpl;

    private static final Pattern RE_VALID_TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    private void validateTableName(String tableName) {
        if (!RE_VALID_TABLE_NAME.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid '" + tableName + "' table");
        }
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Map<String, Object>> getTables() {
        return jdbcTmpl.queryForList(
                        """
                         SELECT table_name
                         FROM information_schema.tables
                         WHERE table_schema = 'public'
                         ORDER BY 1
                        """,
                        Collections.emptyMap())
                .stream()
                .peek(it -> {
                    var q = new NamedParameterQuery(
                            "select count(*)\n" +
                            "from ")
                            .identifier(it.get("table_name").toString());
                    it.put("record_count",
                           jdbcTmpl.queryForObject(q.getStatement(),
                                                   q.getParameters(),
                                                   Long.class));
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{table}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Map<String, Object>> getRecords(
            @PathVariable("table") String tableName
    ) {
        validateTableName(tableName);
        var q = new NamedParameterQuery(
                "select *\n" +
                "from ")
                .identifier(tableName)
                .append("\n" +
                        "order by 1");
        return jdbcTmpl.queryForList(q.getStatement(),
                                     q.getParameters());
    }

    @GetMapping("/{table}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getRecord(
            @PathVariable("table") String tableName,
            @PathVariable("id") Long id
    ) {
        validateTableName(tableName);
        var q = new NamedParameterQuery(
                "select *\n" +
                "from ")
                .identifier(tableName)
                .append("\n" +
                        "where id = ")
                .bind(id);
        return jdbcTmpl.queryForMap(q.getStatement(),
                                    q.getParameters());
    }

}
