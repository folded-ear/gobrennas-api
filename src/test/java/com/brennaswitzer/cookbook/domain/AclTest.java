package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AclTest {

    @Test
    public void owner() {
        Acl acl = new Acl();
        assertNull(acl.getOwner());
        User u = new User();
        acl.setOwner(u);
        assertSame(u, acl.getOwner());
    }

    @Test
    public void grants() {
        Acl acl = new Acl();
        User alice = new User();
        User bob = new User();
        User eve = new User();
        assertNull(acl.setGrant(alice, AccessLevel.ADMINISTER));
        assertNull(acl.setGrant(bob, AccessLevel.VIEW));

        assertEquals(AccessLevel.ADMINISTER, acl.getGrant(alice));
        assertEquals(AccessLevel.VIEW, acl.getGrant(bob));
        assertNull(acl.getGrant(eve));

        acl.deleteGrant(bob);
        assertNull(acl.getGrant(bob));
    }

    @Test
    public void ownerGrants() {
        Acl acl = new Acl();
        User alice = new User();
        assertNull(acl.getGrant(alice)); // sanity

        // owners are always granted ADMINISTER
        acl.setOwner(alice);
        assertEquals(AccessLevel.ADMINISTER, acl.getGrant(alice));

        User bob = new User();

        acl.setOwner(bob);
        assertNull(acl.getGrant(alice));
    }

    @Test
    public void cantAddOwnerGrants() {
        Acl acl = new Acl();
        User alice = new User();
        acl.setOwner(alice);

        assertThrows(UnsupportedOperationException.class, () ->
                acl.setGrant(alice, AccessLevel.VIEW));
    }

    @Test
    public void cantRemoveOwnerGrants() {
        Acl acl = new Acl();
        User alice = new User();
        acl.setOwner(alice);

        assertThrows(UnsupportedOperationException.class, () ->
                acl.deleteGrant(alice));
    }

    @Test
    public void changingOwnersFixesGrants() {
        Acl acl = new Acl();
        User alice = new User();
        User bob = new User();
        acl.setOwner(alice);
        acl.setGrant(bob, AccessLevel.VIEW);

        acl.setOwner(bob);
        assertEquals(AccessLevel.ADMINISTER, acl.getGrant(bob));
        acl.setOwner(alice);

        assertNull(acl.getGrant(bob));
    }
}
