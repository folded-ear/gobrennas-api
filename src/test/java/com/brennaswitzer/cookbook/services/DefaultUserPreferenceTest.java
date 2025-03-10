package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUserPreferenceTest {

    @InjectMocks
    private DefaultUserPreference defPref;

    @Mock
    private GetUserPreference getUserPreference;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PlanRepository planRepo;

    @Test
    void build_simple() {
        User user = mock(User.class);
        Preference preference = mock(Preference.class);
        when(preference.getName()).thenReturn("sumthin");
        when(preference.getDefaultValue()).thenReturn("the val");
        UserDevice device = mock(UserDevice.class);

        var pref = defPref.build(user,
                                 preference,
                                 device);

        assertSame(user, pref.getUser());
        assertSame(preference, pref.getPreference());
        assertSame(device, pref.getDevice());
        assertSame("the val", pref.getValue());
    }

    @Test
    void build_activePlan_user() {
        User user = mock(User.class);
        Preference preference = mock(Preference.class);
        when(preference.getName()).thenReturn(Preference.PREF_ACTIVE_PLAN);
        Plan plan = mock(Plan.class);
        when(plan.getId()).thenReturn(123L);
        when(planRepo.findByOwner(any()))
                .thenReturn(List.of(plan));

        var pref = defPref.build(user,
                                 preference,
                                 (UserDevice) null);

        assertEquals("123", pref.getValue());
        verify(planRepo).findByOwner(user);
        verifyNoMoreInteractions(planRepo);
    }

    @Test
    void build_activePlan_friend() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(456L);
        Preference preference = mock(Preference.class);
        when(preference.getName()).thenReturn(Preference.PREF_ACTIVE_PLAN);
        Plan plan = mock(Plan.class);
        when(plan.getId()).thenReturn(123L);
        when(planRepo.findByOwner(any()))
                .thenReturn(List.of());
        when(planRepo.findAccessiblePlans(any()))
                .thenReturn(List.of(plan));

        var pref = defPref.build(user,
                                 preference,
                                 (UserDevice) null);

        assertEquals("123", pref.getValue());
        verify(planRepo).findByOwner(user);
        verify(planRepo).findAccessiblePlans(456L);
    }

    @Test
    void build_activePlan_none() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(456L);
        Preference preference = mock(Preference.class);
        when(preference.getName()).thenReturn(Preference.PREF_ACTIVE_PLAN);
        when(planRepo.findByOwner(any()))
                .thenReturn(List.of());
        when(planRepo.findAccessiblePlans(any()))
                .thenReturn(List.of());

        var pref = defPref.build(user,
                                 preference,
                                 (UserDevice) null);

        assertNull(pref.getValue());
        verify(planRepo).findByOwner(user);
        verify(planRepo).findAccessiblePlans(456L);
    }

    @Test
    void build_shoppingPlans_active() throws JsonProcessingException {
        User user = mock(User.class);
        Preference preference = mock(Preference.class);
        when(preference.getName()).thenReturn(Preference.PREF_ACTIVE_SHOPPING_PLANS);
        UserPreference pref = mock(UserPreference.class);
        when(pref.getValue()).thenReturn("789");
        when(getUserPreference.find(any(), any(Preference.class), any()))
                .thenReturn(Optional.of(pref));
        when(objectMapper.writeValueAsString(Set.of("789")))
                .thenReturn("json");

        pref = defPref.build(user,
                             preference,
                             (UserDevice) null);

        assertEquals("json", pref.getValue());
        verifyNoInteractions(planRepo);
        verify(getUserPreference).find(user, preference, null);
    }

    @Test
    void build_shoppingPlans_none() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(456L);
        Preference preference = mock(Preference.class);
        when(preference.getName()).thenReturn(Preference.PREF_ACTIVE_SHOPPING_PLANS);
        when(planRepo.findByOwner(any()))
                .thenReturn(List.of());
        when(planRepo.findAccessiblePlans(any()))
                .thenReturn(List.of());
        when(getUserPreference.find(any(), any(Preference.class), any()))
                .thenReturn(Optional.empty());

        var pref = defPref.build(user,
                                 preference,
                                 (UserDevice) null);

        assertNull(pref.getValue());
        verify(planRepo).findByOwner(user);
        verify(planRepo).findAccessiblePlans(456L);
    }

}
