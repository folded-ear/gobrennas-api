package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserPreferenceTest {

    private GetUserPreference getUserPref;

    @BeforeEach
    void setUp() {
        getUserPref = new GetUserPreference();
    }

    @Test
    void unknownPref() {
        User user = mock(User.class);
        UserPreference pref = mock(UserPreference.class);
        when(pref.answersTo(any())).thenReturn(false);
        when(user.getPreferences()).thenReturn(List.of(pref));

        assertTrue(getUserPref.find(user, "glerg", null)
                           .isEmpty());
    }

    @Test
    void unknownDevice() {
        User user = mock(User.class);
        UserDevice device = mock(UserDevice.class);
        UserPreference pref = mock(UserPreference.class);
        when(pref.answersTo(any())).thenReturn(true);
        when(user.getPreferences()).thenReturn(List.of(pref));

        assertTrue(getUserPref.find(user, "glerg", device)
                           .isEmpty());
    }

    @Test
    void mustMatchBoth() {
        User user = mock(User.class);
        UserDevice device = mock(UserDevice.class);
        UserPreference one = mock(UserPreference.class);
        when(one.answersTo(any())).thenReturn(true);
        UserPreference another = mock(UserPreference.class);
        when(another.answersTo(any())).thenReturn(false);
        when(another.getDevice()).thenReturn(device);
        UserPreference three = mock(UserPreference.class);
        when(three.answersTo(any())).thenReturn(true);
        when(three.getDevice()).thenReturn(device);
        when(user.getPreferences()).thenReturn(List.of(
                one,
                another,
                three));

        //noinspection OptionalGetWithoutIsPresent
        assertSame(three,
                   getUserPref.find(user, "glerg", device)
                           .get());
    }

    @Test
    void getErrors_global() {
        User user = mock(User.class);
        UserPreference pref = mock(UserPreference.class);
        when(pref.answersTo(any())).thenReturn(false);
        when(user.getPreferences()).thenReturn(List.of(pref));

        assertThrows(NoResultException.class,
                     () -> getUserPref.get(user, "glerg", null));
    }

    @Test
    void getErrors_device() {
        User user = mock(User.class);
        UserDevice device = mock(UserDevice.class);
        UserPreference pref = mock(UserPreference.class);
        when(pref.answersTo(any())).thenReturn(false);
        when(user.getPreferences()).thenReturn(List.of(pref));

        assertThrows(NoResultException.class,
                     () -> getUserPref.get(user, "glerg", device));
    }

}
