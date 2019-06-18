package com.brennaswitzer.cookbook.domain;

import org.junit.Test;

import static org.junit.Assert.*;

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
        assertNull(acl.setGrant(alice, Permission.ADMINISTER));
        assertNull(acl.setGrant(bob, Permission.VIEW));

        assertEquals(Permission.ADMINISTER, acl.getGrant(alice));
        assertEquals(Permission.VIEW, acl.getGrant(bob));
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
        assertEquals(Permission.ADMINISTER, acl.getGrant(alice));

        User bob = new User();

        acl.setOwner(bob);
        assertNull(acl.getGrant(alice));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cantAddOwnerGrants() {
        Acl acl = new Acl();
        User alice = new User();
        acl.setOwner(alice);

        acl.setGrant(alice, Permission.VIEW);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cantRemoveOwnerGrants() {
        Acl acl = new Acl();
        User alice = new User();
        acl.setOwner(alice);

        acl.deleteGrant(alice);
    }

    @Test
    public void changingOwnersFixesGrants() {
        Acl acl = new Acl();
        User alice = new User();
        User bob = new User();
        acl.setOwner(alice);
        acl.setGrant(bob, Permission.VIEW);

        acl.setOwner(bob);
        assertEquals(Permission.ADMINISTER, acl.getGrant(bob));
        acl.setOwner(alice);

        assertNull(acl.getGrant(bob));
    }
}