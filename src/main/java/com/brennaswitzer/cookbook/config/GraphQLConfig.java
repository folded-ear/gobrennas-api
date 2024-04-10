package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.graphql.support.OffsetConnectionCursorCoercing;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.ResultPath;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.kickstart.execution.context.DefaultGraphQLContext;
import graphql.kickstart.execution.context.GraphQLKickstartContext;
import graphql.kickstart.servlet.apollo.ApolloScalars;
import graphql.kickstart.servlet.context.GraphQLServletContextBuilder;
import graphql.language.OperationDefinition;
import graphql.language.SourceLocation;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    public GraphQLScalarType upload() {
        return ApolloScalars.Upload;
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
    public GraphQLServletContextBuilder graphQLServletContextBuilder(DataLoaderRegistry dataLoadRegistry) {
        return new GraphQLServletContextBuilder() {
            @Override
            public GraphQLKickstartContext build() {
                return new DefaultGraphQLContext(dataLoadRegistry);
            }

            @Override
            public GraphQLKickstartContext build(HttpServletRequest request, HttpServletResponse response) {
                Map<Object, Object> map = new HashMap<>();
                map.put(HttpServletRequest.class, request);
                map.put(HttpServletResponse.class, response);
                return new DefaultGraphQLContext(dataLoadRegistry, map);
            }

            @Override
            public GraphQLKickstartContext build(Session session, HandshakeRequest handshakeRequest) {
                Map<Object, Object> map = new HashMap<>();
                map.put(Session.class, session);
                map.put(HandshakeRequest.class, handshakeRequest);
                return new DefaultGraphQLContext(dataLoadRegistry, map);
            }
        };
    }

    @Bean
    public DataLoaderDispatcherInstrumentation dataLoaderDispatcherInstrumentation() {
        return new DataLoaderDispatcherInstrumentation();
    }

    @Bean
    public DataLoaderRegistry dataLoadRegistry(Collection<BatchLoader<?, ?>> batchLoaders) {
        DataLoaderOptions dataLoaderOptions = new DataLoaderOptions()
                .setCachingEnabled(false)
                .setCachingExceptionsEnabled(false)
                .setMaxBatchSize(10_000);
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        batchLoaders.forEach(l -> dataLoaderRegistry.register(
                l.getClass().getName(),
                DataLoaderFactory.newDataLoader(l, dataLoaderOptions)));
        return dataLoaderRegistry;
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

     The query/mutation fork is added, so Hibernate can optimize for read-only
     transactions. Sort of an awkward place to put it, but can't use Spring's
     annotations with imperative demarcation, so don't really have a choice.
    */
    @Bean
    ExecutionStrategy transactionalExecutionStrategy(
            TransactionTemplate mutationTmpl
    ) {
        // Making this a proper bean gave cyclic dependency errors for reasons
        // I decided not to try and figure out. The warning is for IntelliJ's
        // massive over-aggro application of @NotNull to places where library
        // authors omitted it. In this case, Spring explicitly says the passed
        // definition must be non-null, but leaves the manager nullable. But
        // IntelliJ doesn't care about Spring's opinion, so have to suppress it.
        @SuppressWarnings("DataFlowIssue")
        TransactionTemplate queryTmpl = new TransactionTemplate(
                mutationTmpl.getTransactionManager(),
                mutationTmpl);
        queryTmpl.setReadOnly(true);
        return new AsyncExecutionStrategy() {
            @Override
            public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext,
                                                              ExecutionStrategyParameters parameters) {
                TransactionTemplate txTmpl = executionContext.getOperationDefinition()
                        .getOperation() == OperationDefinition.Operation.QUERY
                        ? queryTmpl
                        : mutationTmpl;
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
