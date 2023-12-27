package com.brennaswitzer.cookbook.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates the test target / component under test for a {@link MockTest}
 * subtype, which should be wired up with {@link org.mockito.Mock @Mock}-ed
 * collaborators from other instance fields.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MockTestTarget {
}
