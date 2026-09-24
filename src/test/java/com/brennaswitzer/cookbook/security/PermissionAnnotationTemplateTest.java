package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * I show how a templated {@code @PreAuthorize} meta-annotation renders an
 * enum attribute and a SpEL id expression into a {@code hasPermission}
 * check.
 */
@SpringJUnitConfig(PermissionAnnotationTemplateTest.Config.class)
@WithMockUser
class PermissionAnnotationTemplateTest {

    private static final String TARGET_TYPE = "Probe";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @PreAuthorize("hasPermission({id}, 'Probe', '{level}')")
    @interface ProbeAccess {

        String id();

        AccessLevel level();

    }

    @Configuration
    @EnableMethodSecurity
    static class Config {

        @Bean
        static AnnotationTemplateExpressionDefaults templateDefaults() {
            return new AnnotationTemplateExpressionDefaults();
        }

        @Bean
        RecordingPermissionEvaluator recordingPermissionEvaluator() {
            return new RecordingPermissionEvaluator();
        }

        @Bean
        static MethodSecurityExpressionHandler expressionHandler(
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

        @ProbeAccess(id = "#id", level = AccessLevel.CHANGE)
        public void single(Long id) {
        }

        @ProbeAccess(id = "{#bucketId, #afterId}", level = AccessLevel.VIEW)
        public void pair(Long bucketId, Long afterId) {
        }

    }

    @Autowired
    private Guarded guarded;

    @Autowired
    private RecordingPermissionEvaluator evaluator;

    @Test
    void singleId() {
        guarded.single(3L);

        assertEquals(3L, evaluator.getTargetId());
        assertEquals(TARGET_TYPE, evaluator.getTargetType());
        assertEquals(AccessLevel.CHANGE.name(), evaluator.getPermission());
    }

    @Test
    void idListWithNull() {
        guarded.pair(1L, null);

        assertEquals(Arrays.asList(1L, null), evaluator.getTargetId());
        assertEquals(AccessLevel.VIEW.name(), evaluator.getPermission());
    }

    @Test
    void evaluatorExceptionPropagatesUnwrapped() {
        evaluator.failWith(new EntityNotFoundException());
        try {
            assertThrows(EntityNotFoundException.class,
                         () -> guarded.single(3L));
        } finally {
            evaluator.failWith(null);
        }
    }

}
