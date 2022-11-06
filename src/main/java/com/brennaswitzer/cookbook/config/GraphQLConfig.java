package com.brennaswitzer.cookbook.config;

import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Configuration
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
    public GraphQLScalarType nonNegativeFloat() {
        return ExtendedScalars.NonNegativeFloat;
    }

    /*
     These two beans are taken from https://blog.akquinet.de/2020/04/16/part-2-graphql-with-spring-boot-jpa-and-kotlin/
     as a way to have "open session in view" behaviour across a GraphQL query.
     Not the normal sort of thing, since usually GraphQL is used to front one or
     more services which actually do data access, but in this case, our graph
     is actually a single JPA context. Thus, lazy-loading is a core feature, and
     a single transaction per query is necessary.
    */
    @Bean
    AsyncExecutionStrategy asyncTransactionalExecutionStrategyService() {
        return new AsyncExecutionStrategy() {
            @Override
            @Transactional
            public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {
                return super.execute(executionContext, parameters);
            }
        };
    }

    @Bean
    Map<String, ExecutionStrategy> executionStrategies(
            AsyncExecutionStrategy asyncTransactionalExecutionStrategyService
    ) {
        val executionStrategyMap = new HashMap<String, ExecutionStrategy>();
        executionStrategyMap.put("queryExecutionStrategy", asyncTransactionalExecutionStrategyService);
        return executionStrategyMap;
    }

}
