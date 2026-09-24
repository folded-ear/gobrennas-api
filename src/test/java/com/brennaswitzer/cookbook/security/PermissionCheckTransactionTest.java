package com.brennaswitzer.cookbook.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I show that a {@code hasPermission} check runs inside the
 * {@code @Transactional} method it guards, under the app's own interceptor
 * ordering.
 */
@SpringBootTest
@DisabledIfSystemProperty(named = "test-containers", matches = "disabled")
@WithMockUser
class PermissionCheckTransactionTest {

    @TestConfiguration
    static class Config {

        @Bean
        RecordingPermissionEvaluator recordingPermissionEvaluator() {
            return new RecordingPermissionEvaluator();
        }

        @Bean
        @Primary
        static MethodSecurityExpressionHandler recordingExpressionHandler(
                RecordingPermissionEvaluator evaluator) {
            var handler = new DefaultMethodSecurityExpressionHandler();
            handler.setPermissionEvaluator(evaluator);
            return handler;
        }

        @Bean
        Guarded guarded() {
            return new Guarded();
        }

    }

    static class Guarded {

        @Transactional
        @PreAuthorize("hasPermission(0L, 'Probe', 'VIEW')")
        public boolean run() {
            return RecordingPermissionEvaluator.isTransactionActive();
        }

    }

    @Autowired
    private Guarded guarded;

    @Autowired
    private RecordingPermissionEvaluator evaluator;

    @Test
    void checkRunsInsideTheTransaction() {
        assertTrue(guarded.run(),
                   "method body should be in a transaction");
        assertTrue(evaluator.wasCalled(),
                   "evaluator should have been called");
        assertTrue(evaluator.wasTransactionActive(),
                   "check should run inside the transaction");
    }

}
