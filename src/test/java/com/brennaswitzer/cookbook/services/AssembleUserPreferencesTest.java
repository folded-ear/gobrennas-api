package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssembleUserPreferencesTest {

    @InjectMocks
    private AssembleUserPreferences assemble;

    @Mock
    private DefaultUserPreference defaultUserPreference;

    @Mock
    private PreferenceRepository preferenceRepo;

    @Test
    void assemble() {
        Preference a = pref("A");
        Preference b = pref("B");
        Preference c = pref("C");
        User user = new User();
        UserDevice d1 = new UserDevice();
        UserDevice d2 = new UserDevice();
        when(preferenceRepo.findAll()).thenReturn(List.of(a, b, c));
        user.setPreferences(List.of(
                pref(a, d1, "one"),
                pref(a, d2, "two"),
                pref(b, d2, "three")));
        AtomicInteger counter = new AtomicInteger();
        when(defaultUserPreference.factory(user, d1))
                .thenReturn(p -> {
                    counter.incrementAndGet();
                    return pref(p, d1, p.getDefaultValue());
                });

        Collection<UserPreference> result = assemble.assemble(user, d1);

        assertEquals(3, result.size());
        assertEquals(List.of(a, b, c),
                     result.stream().map(UserPreference::getPreference).toList());
        assertEquals(List.of("one", "B", "C"),
                     result.stream().map(UserPreference::getValue).toList());
        assertEquals(2, counter.get());
    }

    private Preference pref(String defVal) {
        Preference mock = new Preference();
        mock.setDefaultValue(defVal);
        return mock;
    }

    private UserPreference pref(Preference p, UserDevice d, String val) {
        UserPreference mock = new UserPreference();
        mock.setPreference(p);
        mock.setDevice(d);
        mock.setValue(val);
        return mock;
    }

}
