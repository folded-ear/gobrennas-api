package com.brennaswitzer.cookbook.domain;

public interface Named {

    String getName();

    /**
     * Whether this Named answers to the given name. The default implementation
     * simply checks case-insensitive equality. No Named ever answers to null.
     */
    default boolean answersToName(String name) {
        return name != null && name.equalsIgnoreCase(getName());
    }
    
}
