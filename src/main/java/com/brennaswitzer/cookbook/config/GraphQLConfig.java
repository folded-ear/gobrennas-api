package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.graphql.support.OffsetConnectionCursorCoercing;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

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
    @Bean
    ExecutionStrategy transactionalExecutionStrategy(
            TransactionTemplate txTmpl
    ) {
        return new AsyncExecutionStrategy() {
            @Override
            public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {
                return txTmpl.execute(tx -> {
                    val result = super.execute(executionContext, parameters);
                    if (tx.isNewTransaction() && tx instanceof DefaultTransactionStatus) {
                        if (((DefaultTransactionStatus) tx).isGlobalRollbackOnly()) {
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
