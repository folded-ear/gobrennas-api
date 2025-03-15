package com.brennaswitzer.cookbook.graphql.instrumentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQLLoggingInstrumentation extends SimplePerformantInstrumentation {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLLoggingInstrumentation.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters,
            InstrumentationState state) {
        if (!logger.isInfoEnabled()) return SimpleInstrumentationContext.noOp();
        var startMillis = System.currentTimeMillis();
        var debugEnabled = logger.isDebugEnabled();
        var executionId = parameters.getExecutionInput().getExecutionId();
        if (debugEnabled) {
            logger.debug("graphql {} query: \"{}\"",
                         executionId,
                         StringEscapeUtils.escapeJson(parameters.getQuery().strip()));
            logger.debug("graphql {} variables: {}",
                         executionId,
                         maybeAsJson(parameters.getVariables()));
        }
        return new SimpleInstrumentationContext<>() {
            @Override
            public void onCompleted(ExecutionResult executionResult, Throwable t) {
                var elapsed = System.currentTimeMillis() - startMillis;
                if (t != null) {
                    // SLF4J can EITHER interpolate OR log a Throwable.
                    logger.error("graphql %s exception:".formatted(executionId), t);
                }
                for (var e : executionResult.getErrors()) {
                    logger.warn("graphql {} error: {}",
                                executionId,
                                e);
                }
                if (debugEnabled && executionResult.isDataPresent()) {
                    logger.debug("graphql {} data: {}",
                                 executionId,
                                 maybeAsJson(executionResult.<Object>getData()));
                }
                logger.info("graphql {} operation: {} {}ms",
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
