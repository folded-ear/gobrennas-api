package com.brennaswitzer.cookbook.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class NamedParameterQuery {

    private final StringBuilder statement = new StringBuilder();

    private final Map<String, Object> params = new HashMap<>();

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
        return statement.toString();
    }

    public Map<String, Object> getParameters() {
        return params;
    }

    public void forEachParameter(BiConsumer<String, Object> action) {
        params.forEach(action);
    }

}
