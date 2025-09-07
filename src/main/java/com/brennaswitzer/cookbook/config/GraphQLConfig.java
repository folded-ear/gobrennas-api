package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.graphql.support.CachingPreparsedDocumentProvider;
import com.brennaswitzer.cookbook.graphql.support.OffsetConnectionCursorCoercing;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.ResultPath;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.language.SourceLocation;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Collection;
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
            ObjectProvider<DataFetcherExceptionResolver> resolvers) {
        // The warning is for IntelliJ's over-aggro application of @NotNull to
        // places where library authors omitted it. In this case, Spring
        // explicitly says the passed definition must be non-null, but leaves
        // the manager nullable. IntelliJ doesn't care about Spring's opinion,
        // so have to suppress it.
        @SuppressWarnings("DataFlowIssue")
        TransactionTemplate queryTmpl = new TransactionTemplate(
                mutationTmpl.getTransactionManager(),
                mutationTmpl);

        DataFetcherExceptionHandler exceptionHandler =
                DataFetcherExceptionResolver.createExceptionHandler(resolvers.stream().toList());

        ExecutionStrategy queryStrat = transactionalExecutionStrategy(queryTmpl, exceptionHandler);
        ExecutionStrategy mutationStrat = transactionalExecutionStrategy(mutationTmpl, exceptionHandler);

        return sourceBuilder -> sourceBuilder
                .configureGraphQl(builder -> builder
                        .queryExecutionStrategy(queryStrat)
                        .mutationExecutionStrategy(mutationStrat)
                        .preparsedDocumentProvider(preparsedDocumentProvider()))
                .configureRuntimeWiring(wiring -> scalars.forEach(wiring::scalar))
                .inspectSchemaMappings(report -> log.info("{}", report));
    }

    /*
     This bean is taken from https://blog.akquinet.de/2020/04/16/part-2-graphql-with-spring-boot-jpa-and-kotlin/
     as a way to have "open session in view" behaviour across a GraphQL query.
     Not the normal sort of thing, since usually GraphQL is used to front one or
     more services which actually do data access, but in this case, our graph
     is actually a single JPA context. Thus, lazy-loading is a core feature, and
     a single transaction per query is necessary.

     The shenanigans w/ global rollback emulate what Spring's
     AbstractPlatformTransactionManager does if Hibernate has marked the
     SmartTransaction as rollback-only, rather than the TransactionStatus. Out
     of the box, such a state will mask the underlying exception(s) to the
     GraphQL client w/ a generic "silently rolled back" error. By manually
     propagating the rollback-only state, Spring doesn't do the "smart" check or
     raise an Exception, because it's now explicit.
    */
    private ExecutionStrategy transactionalExecutionStrategy(
            TransactionTemplate txTmpl,
            DataFetcherExceptionHandler exceptionHandler
    ) {
        return new AsyncExecutionStrategy(exceptionHandler) {
            @Override
            public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext,
                                                              ExecutionStrategyParameters parameters) {
                return txTmpl.execute(tx -> {
                    val result = super.execute(executionContext, parameters);
                    if (tx.isNewTransaction() && tx instanceof DefaultTransactionStatus dtx) {
                        if (dtx.isGlobalRollbackOnly()) {
                            tx.setRollbackOnly();
                            // What Spring would have raised
                            val ure = new UnexpectedRollbackException(
                                    "Transaction silently rolled back because it has been marked as rollback-only");
                            log.error(ure.getMessage(), ure);
                            // This doesn't actually make it into the response
                            // for unknown reasons, but does update the context.
                            executionContext.addError(new ExceptionWhileDataFetching(
                                    ResultPath.rootPath().segment(""),
                                    ure,
                                    new SourceLocation(1, 1)));
                        }
                    }
                    return result;
                });
            }
        };
    }

}
