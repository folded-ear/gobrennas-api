package com.brennaswitzer.cookbook.domain;

/**
 * I represent a level of access, in a hierarchical fashion. That is, every
 * AccessLevel implicitly includes all lower levels (listed earlier in the
 * enumeration). For example, user who has been granted the {@link #ADMINISTER}
 * level will be considered to have the {@link #VIEW} as well.
 */
public enum AccessLevel {
    VIEW,
    CHANGE,
    ADMINISTER;

    public boolean includes(AccessLevel other) {
        return other.ordinal() <= ordinal();
    }

}
