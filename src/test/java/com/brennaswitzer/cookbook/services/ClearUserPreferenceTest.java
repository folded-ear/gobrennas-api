package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PreferenceRepository;
import com.brennaswitzer.cookbook.repositories.UserPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearUserPreferenceTest {

    @InjectMocks
    private ClearUserPreference clearPref;

    @Mock
    private GetUserPreference getUserPreference;
    @Mock
    private DefaultUserPreference defaultUserPreference;

    @Mock
    private PreferenceRepository preferenceRepo;
    @Mock
    private UserPreferenceRepository userPrefRepo;

    @Test
    void existing() {
        User user = mock(User.class);
        @SuppressWarnings("unchecked")
        Collection<UserPreference> prefs = mock(List.class);
        when(user.getPreferences()).thenReturn(prefs);
        Preference preference = mock(Preference.class);
        UserPreference oldPref = mock(UserPreference.class);
        when(oldPref.getPreference()).thenReturn(preference);
        when(oldPref.getUser()).thenReturn(user);
        when(getUserPreference.find(any(), anyString(), any()))
                .thenReturn(Optional.of(oldPref));
        UserPreference pref = mock(UserPreference.class);
        when(defaultUserPreference.build(user, preference, (UserDevice) null))
                .thenReturn(pref);

        var result = clearPref.clear(user, "goob", (UserDevice) null);

        assertSame(pref, result);
        verify(oldPref).setUser(null);
        verify(prefs).remove(oldPref);
        verifyNoInteractions(preferenceRepo);
    }

    @Test
    void unset() {
        User user = mock(User.class);
        UserDevice device = mock(UserDevice.class);
        Preference preference = mock(Preference.class);
        when(getUserPreference.find(any(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(preferenceRepo.getByName("goob"))
                .thenReturn(preference);
        UserPreference pref = mock(UserPreference.class);
        when(defaultUserPreference.build(user, preference, device))
                .thenReturn(pref);

        var result = clearPref.clear(user, "goob", device);

        assertSame(pref, result);
        verify(getUserPreference).find(user, "goob", device);
        verify(preferenceRepo).getByName("goob");
        verifyNoInteractions(userPrefRepo);
    }

}
