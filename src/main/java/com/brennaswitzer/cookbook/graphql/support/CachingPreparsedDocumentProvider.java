package com.brennaswitzer.cookbook.graphql.support;

import com.github.benmanes.caffeine.cache.Cache;
import graphql.ExecutionInput;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;

import java.util.function.Function;

public class CachingPreparsedDocumentProvider implements PreparsedDocumentProvider {

    private final Cache<Object, PreparsedDocumentEntry> cache;

    public CachingPreparsedDocumentProvider(Cache<Object, PreparsedDocumentEntry> cache) {
        this.cache = cache;
    }

    @Override
    public PreparsedDocumentEntry getDocument(
            ExecutionInput executionInput,
            Function<ExecutionInput, PreparsedDocumentEntry> computeFunction) {
        return cache.get(executionInput.getQuery(),
                         k -> computeFunction.apply(executionInput));
    }

}
