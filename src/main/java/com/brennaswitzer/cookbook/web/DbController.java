package com.brennaswitzer.cookbook.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SqlResolve"})
@RestController
@RequestMapping("api/_db")
@PreAuthorize("hasRole('DEVELOPER')")
public class DbController {

    @Autowired
    private JdbcTemplate tmpl;

    private static final Pattern RE_VALID_TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    private void validateTableName(String tableName) {
        if (!RE_VALID_TABLE_NAME.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid '" + tableName + "' table");
        }
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Map<String, Object>> getTables() {
        return tmpl.queryForList("select table_name\n" +
                "from information_schema.tables\n" +
                "where table_schema = 'public'\n" +
                "order by 1")
                .stream()
                .peek(it -> it.put("record_count", tmpl
                        .queryForObject("select count(*)\n" +
                                "from " + it.get("table_name"), Integer.class)))
                .collect(Collectors.toList());
    }

    @GetMapping("/{table}")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Map<String, Object>> getRecords(
            @PathVariable("table") String tableName
    ) {
        validateTableName(tableName);
        return tmpl.queryForList("select *\n" +
                "from " + tableName + "\n" +
                "order by 1");
    }

    @GetMapping("/{table}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getRecord(
            @PathVariable("table") String tableName,
            @PathVariable("id") Long id
    ) {
        validateTableName(tableName);
        return tmpl.queryForMap("select *\n" +
                "from " + tableName + "\n" +
                "where id = ?", id);
    }

}
