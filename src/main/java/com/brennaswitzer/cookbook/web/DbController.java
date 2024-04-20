package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.services.indexing.IndexStats;
import com.brennaswitzer.cookbook.services.indexing.IngredientFulltextIndexer;
import com.brennaswitzer.cookbook.services.indexing.IngredientReindexQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({ "SpringJavaAutowiredFieldsWarningInspection", "SqlResolve" })
@RestController
@RequestMapping("api/_db")
@PreAuthorize("hasRole('DEVELOPER')")
public class DbController {

    @Autowired
    private JdbcTemplate tmpl;

    @Autowired
    private IngredientReindexQueueService ingredientReindexQueueService;

    @Autowired
    private IngredientFulltextIndexer ingredientFulltextIndexer;

    private static final Pattern RE_VALID_TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    private void validateTableName(String tableName) {
        if (!RE_VALID_TABLE_NAME.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid '" + tableName + "' table");
        }
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<Map<String, Object>> getTables() {
        return tmpl.queryForList("""
                                 select table_name
                                 from information_schema.tables
                                 where table_schema = 'public'
                                 order by 1
                                 """)
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

    @GetMapping("/ingredient-index")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public IndexStats getIndexStats() {
        return ingredientReindexQueueService.getIndexStats();
    }

    @GetMapping("/ingredient-index/drain-queue")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public IndexStats reindex() {
        ingredientFulltextIndexer.reindexQueued();
        return getIndexStats();
    }

}
