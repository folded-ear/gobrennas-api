package com.brennaswitzer.cookbook.util;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@DisabledIfSystemProperty(named = "test-containers", matches = "disabled")
@Transactional
@WithSecurityContext(factory = AliceBobEveContext.class)
public @interface WithAliceBobEve {

    boolean authentication() default true;

}
