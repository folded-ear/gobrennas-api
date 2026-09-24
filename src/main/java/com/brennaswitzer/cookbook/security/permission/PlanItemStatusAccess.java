package com.brennaswitzer.cookbook.security.permission;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * I require that the current user may set a plan item's status.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PreAuthorize("hasPermission({id}, 'PlanItem', {status})")
public @interface PlanItemStatusAccess {

    /**
     * I am a SpEL expression for the id(s) to check, e.g., {@code "#id"}.
     */
    String id();

    /**
     * I am a SpEL expression for the status to set, e.g., {@code "#status"}.
     */
    String status();

}
