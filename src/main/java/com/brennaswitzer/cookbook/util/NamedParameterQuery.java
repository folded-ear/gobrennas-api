package com.brennaswitzer.cookbook.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class NamedParameterQuery {

    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*");

    private final StringBuilder statement = new StringBuilder();

    private final Map<String, Object> params = new HashMap<>();

    private String statementString;

    public NamedParameterQuery() {
    }

    public NamedParameterQuery(String queryFragment) {
        append(queryFragment);
    }

    public NamedParameterQuery(String queryFragment,
                               Map<String, Object> params) {
        append(queryFragment, params);
    }

    public NamedParameterQuery(String queryFragment,
                               String paramName,
                               Object paramValue) {
        append(queryFragment, paramName, paramValue);
    }

    public NamedParameterQuery append(String queryFragment) {
        this.statement.append(queryFragment);
        this.statementString = null;
        return this;
    }

    public NamedParameterQuery append(String queryFragment,
                                      Map<String, Object> params) {
        append(queryFragment);
        params.forEach(this::addParam);
        return this;
    }

    public NamedParameterQuery append(String queryFragment,
                                      String paramName,
                                      Object paramValue) {
        append(queryFragment);
        addParam(paramName, paramValue);
        return this;
    }

    public NamedParameterQuery append(NamedParameterQuery query) {
        append(query.getStatement());
        query.forEachParameter(this::addParam);
        return this;
    }

    /**
     * I work like {@link #append(String)}, but will refuse any non-identifier
     * value. When you MUST use dynamic SQL, this offers a modicum of safety
     * against injection attacks.
     */
    public NamedParameterQuery identifier(String id) {
        if (!IDENTIFIER.matcher(id).matches()) {
            throw new IllegalArgumentException(String.format(
                    "Non-identifier '%s' found!",
                    id));
        }
        return append(id);
    }

    public NamedParameterQuery bind(Object paramValue) {
        String name = "p" + (params.size() + 1);
        return append(":" + name, name, paramValue);
    }

    private void addParam(String paramName,
                          Object paramValue) {
        if (params.containsKey(paramName)) {
            throw new IllegalArgumentException(String.format("A '%s' parameter is already defined (set to '%s').",
                                                             paramName,
                                                             params.get(paramName)));
        }
        params.put(paramName, paramValue);
    }

    public String getStatement() {
        String stmt = statementString;
        if (stmt == null) {
            stmt = statement.toString();
            statementString = stmt;
        }
        return stmt;
    }

    public Map<String, Object> getParameters() {
        return params;
    }

    public void forEachParameter(BiConsumer<String, Object> action) {
        params.forEach(action);
    }

}
