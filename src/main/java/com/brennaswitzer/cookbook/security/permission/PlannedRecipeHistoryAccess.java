package com.brennaswitzer.cookbook.security.permission;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * I require that the current user has a level of access to a planned recipe history.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PreAuthorize("hasPermission({id}, 'PlannedRecipeHistory', '{level}')")
public @interface PlannedRecipeHistoryAccess {

    /**
     * I am a SpEL expression for the id(s) to check, e.g., {@code "#id"}.
     */
    String id();

    AccessLevel level();

}
