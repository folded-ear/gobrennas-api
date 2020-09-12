package com.brennaswitzer.cookbook.domain;

/**
 * I represent a level of access, in a hierarchical fashion. That is, every
 * AccessLevel implicitly includes all lower levels (listed earlier in the
 * enumeration). For example, user who has been granted the {@link #ADMINISTER}
 * level will be considered to have the {@link #VIEW} as well.
 */
public enum AccessLevel implements Identified {

    /*
     * These are roughly aligned with Spring Security's permission's bits, in
     * case we ever end up going there. And if we don't, they're just as
     * reasonable as any other arbitrary integers. :)
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    VIEW        (1L << 0), // read
    CHANGE      (1L << 1), // write
    // create
    // delete
    ADMINISTER  (1L << 4); // administration

    private final Long id;

    AccessLevel(Long id) {
        this.id = id;
    }

    public boolean includes(AccessLevel other) {
        return other.ordinal() <= ordinal();
    }

    @Override
    public Long getId() {
        return id;
    }

}
