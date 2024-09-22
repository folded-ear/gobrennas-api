package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class OwnedTest {

    @Data
    @AllArgsConstructor
    private static class TestOwned implements Owned {

        private User owner;

    }

    @Test
    void isOwner_long() {
        var owned = new TestOwned(mockUser(123L));
        assertTrue(owned.isOwner(123L));
        assertFalse(owned.isOwner(456L));
    }

    @Test
    void isOwner_principal() {
        var owned = new TestOwned(mockUser(123L));
        assertTrue(owned.isOwner(mockPrincipal(123L)));
        assertFalse(owned.isOwner(mockPrincipal(456L)));
    }

    @Test
    void isOwner_user() {
        var owned = new TestOwned(mockUser(123L));
        assertTrue(owned.isOwner(mockUser(123L)));
        assertFalse(owned.isOwner(mockUser(456L)));
    }

    @Test
    void user_owns() {
        var user = spy(new User());
        var owned = mock(Owned.class);
        when(owned.isOwner(any(User.class))).thenReturn(false);

        assertFalse(user.owns(owned));

        verify(owned).isOwner(user);
        verifyNoMoreInteractions(owned);
    }

    private static UserPrincipal mockPrincipal(long id) {
        var mock = mock(UserPrincipal.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private static User mockUser(long id) {
        var mock = mock(User.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

}
