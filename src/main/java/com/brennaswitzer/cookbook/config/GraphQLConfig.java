package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.graphql.support.CachingPreparsedDocumentProvider;
import com.brennaswitzer.cookbook.graphql.support.OffsetConnectionCursorCoercing;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@Slf4j
public class GraphQLConfig {

    @Bean
    public GraphQLScalarType date() {
        return ExtendedScalars.Date;
    }

    @Bean
    public GraphQLScalarType dateTime() {
        return ExtendedScalars.DateTime;
    }

    @Bean
    public GraphQLScalarType long_() {
        return ExtendedScalars.GraphQLLong;
    }

    @Bean
    public GraphQLScalarType positiveInt() {
        return ExtendedScalars.PositiveInt;
    }

    @Bean
    public GraphQLScalarType nonNegativeInt() {
        return ExtendedScalars.NonNegativeInt;
    }

    @Bean
    public GraphQLScalarType nonNegativeFloat() {
        return ExtendedScalars.NonNegativeFloat;
    }

    @Bean
    public GraphQLScalarType cursor(OffsetConnectionCursorCoercing coercing) {
        return GraphQLScalarType.newScalar()
                .name("Cursor")
                .description("The type of a cursor, an opaque string used for walking connections")
                .coercing(coercing)
                .build();
    }

    @Bean
    PreparsedDocumentProvider preparsedDocumentProvider() {
        return new CachingPreparsedDocumentProvider(
                Caffeine.newBuilder()
                        // There aren't very many distinct queries
                        .maximumSize(1_000)
                        .expireAfterAccess(Duration.ofHours(6))
                        .scheduler(Scheduler.systemScheduler())
                        .build());
    }

    @Bean
    GraphQlSourceBuilderCustomizer sourceBuilderCustomizer(
            Collection<GraphQLScalarType> scalars,
            TransactionTemplate mutationTmpl,
            DataFetcherExceptionHandler exceptionHandler) {

        // The warning is for IntelliJ's over-aggro application of @NotNull to
        // places where library authors omitted it. In this case, Spring
        // explicitly says the passed definition must be non-null, but leaves
        // the manager nullable. IntelliJ doesn't care about Spring's opinion,
        // so have to suppress it.
        @SuppressWarnings("DataFlowIssue")
        TransactionTemplate queryTmpl = new TransactionTemplate(
                mutationTmpl.getTransactionManager(),
                mutationTmpl);

        // Two separate strategy instances are needed, even though they do the
        // same thing, or the connection pool ends up immediately starved. Not
        // sure what that's about, but it's consistent, so don't do it.
        ExecutionStrategy queryStrat = transactionalExecutionStrategy(
                queryTmpl,
                exceptionHandler);
        ExecutionStrategy mutationStrat = transactionalExecutionStrategy(
                mutationTmpl,
                exceptionHandler);

        return sourceBuilder -> sourceBuilder
                .configureGraphQl(builder -> builder
                        .queryExecutionStrategy(queryStrat)
                        .mutationExecutionStrategy(mutationStrat)
                        .preparsedDocumentProvider(preparsedDocumentProvider()))
                .configureRuntimeWiring(wiring -> scalars.forEach(wiring::scalar))
                .inspectSchemaMappings(report -> log.info("{}", report));
    }

    @Bean
    DataFetcherExceptionHandler exceptionHandler(
            List<DataFetcherExceptionResolver> resolvers) {
        return DataFetcherExceptionResolver.createExceptionHandler(resolvers);
    }

    /*
     This bean is taken from https://blog.akquinet.de/2020/04/16/part-2-graphql-with-spring-boot-jpa-and-kotlin/
     as a way to have "open session in view" behaviour across a GraphQL query.
     Not the normal sort of thing, since usually GraphQL is used to front one or
     more services which actually do data access, but in this case, our graph
     is actually a single JPA context. Thus, lazy-loading is a core feature, and
     a single transaction per query is necessary.
    */
    private ExecutionStrategy transactionalExecutionStrategy(
            TransactionTemplate txTmpl,
            DataFetcherExceptionHandler exceptionHandler
    ) {
        return new AsyncExecutionStrategy(exceptionHandler) {
            @Override
            public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext,
                                                              ExecutionStrategyParameters parameters) {
                MutableObject<CompletableFuture<ExecutionResult>> ref = new MutableObject<>();
                try {
                    txTmpl.executeWithoutResult(tx -> {
                        ref.setValue(super.execute(executionContext, parameters));
                    });
                } catch (UnexpectedRollbackException ure) {
                    // If the execution didn't return a result, something weird
                    // is going on, so rethrow.
                    if (ref.getValue() == null) {
                        throw ure;
                    }
                    // Otherwise the execution was successful (almost certainly
                    // with errors), so log and return the result to the client.
                    log.warn("Unexpected rollback", ure);
                }
                return ref.getValue();
            }
        };
    }

}
