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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetUserPreferenceTest {

    @InjectMocks
    private SetUserPreference setPref;

    @Mock
    private GetUserPreference getUserPreference;

    @Mock
    private PreferenceRepository preferenceRepo;
    @Mock
    private UserPreferenceRepository userPrefRepo;

    @Test
    void updateExisting() {
        User user = mock(User.class);
        UserDevice device = mock(UserDevice.class);
        UserPreference oldPref = mock(UserPreference.class);
        when(getUserPreference.find(any(), anyString(), any()))
                .thenReturn(Optional.of(oldPref));
        when(userPrefRepo.save(any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var pref = setPref.set(user, "goob", device, "the val");

        assertSame(oldPref, pref);
        verify(pref).setValue("the val");
        verify(getUserPreference).find(user, "goob", device);
        verify(userPrefRepo).save(oldPref);
        verifyNoInteractions(preferenceRepo);
    }

    @Test
    void createNew() {
        User user = mock(User.class);
        UserDevice device = mock(UserDevice.class);
        Preference preference = mock(Preference.class);
        when(getUserPreference.find(any(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(preferenceRepo.getByName("goob"))
                .thenReturn(preference);
        when(userPrefRepo.save(any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var pref = setPref.set(user, "goob", device, "the val");

        assertSame(user, pref.getUser());
        assertSame(device, pref.getDevice());
        assertSame(preference, pref.getPreference());
        assertEquals("the val", pref.getValue());
        verify(getUserPreference).find(user, "goob", device);
        verify(userPrefRepo).save(pref);
    }

}
