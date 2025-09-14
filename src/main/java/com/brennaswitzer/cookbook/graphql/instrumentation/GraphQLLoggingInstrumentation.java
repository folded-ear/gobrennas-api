package com.brennaswitzer.cookbook.graphql.instrumentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GraphQLLoggingInstrumentation extends SimplePerformantInstrumentation {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {
        if (!log.isInfoEnabled()) return SimpleInstrumentationContext.noOp();
        return new InstrumentationContext<>() {

            private final boolean debugEnabled = log.isDebugEnabled();
            private final Object executionId = parameters.getExecutionInput().getExecutionId();

            private long startMillis;

            @Override
            public void onDispatched() {
                startMillis = System.currentTimeMillis();
                if (debugEnabled) {
                    log.debug("graphql {} query: \"{}\"",
                              executionId,
                              StringEscapeUtils.escapeJson(parameters.getQuery().strip()));
                    log.debug("graphql {} variables: {}",
                              executionId,
                              maybeAsJson(parameters.getVariables()));
                }
            }

            @Override
            public void onCompleted(ExecutionResult executionResult, Throwable t) {
                var elapsed = System.currentTimeMillis() - startMillis;
                if (t != null) {
                    // SLF4J can EITHER interpolate OR log a Throwable.
                    //noinspection StringConcatenationArgumentToLogCall
                    log.error("graphql %s exception:".formatted(executionId), t);
                }
                for (var e : executionResult.getErrors()) {
                    log.warn("graphql {} error: {}",
                             executionId,
                             e);
                }
                if (debugEnabled && executionResult.isDataPresent()) {
                    log.debug("graphql {} data: {}",
                              executionId,
                              maybeAsJson(executionResult.<Object>getData()));
                }
                log.info("graphql {} operation: {} {}ms",
                         executionId,
                         parameters.getOperation(),
                         elapsed);
            }
        };
    }

    private Object maybeAsJson(Object o) {
        return o instanceof Map<?, ?> m
                ? maybeAsJson(m)
                : o;
    }

    private Object maybeAsJson(Map<?, ?> m) {
        try {
            return objectMapper.writeValueAsString(m);
        } catch (JsonProcessingException ignored) {}
        return m;
    }

}
