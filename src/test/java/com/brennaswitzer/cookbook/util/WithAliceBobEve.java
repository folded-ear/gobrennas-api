package com.brennaswitzer.cookbook.util;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = AliceBobEveContext.class)
public @interface WithAliceBobEve {

    String value() default "Alice";

    boolean authentication() default true;

}
