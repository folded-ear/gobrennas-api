package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.PreferenceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithAliceBobEve
class DefaultUserPreferenceDbTest {

    private static final String DEVICE_KEY = "alices-phone";
    private static final String UNKNOWN_PLAN_ID = "-1";
    private static final String NOT_AN_ID = "garbage";

    @Autowired
    private DefaultUserPreference defPref;

    @Autowired
    private SetUserPreference setUserPreference;

    @Autowired
    private EnsureUserDevice ensureUserDevice;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PreferenceRepository preferenceRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private User alice, bob;

    static List<String> activePlanFollowers() {
        return List.of(Preference.PREF_ACTIVE_SHOPPING_PLANS,
                       Preference.PREF_PLANNER_PLANS);
    }

    @BeforeEach
    void setUp() {
        // the fixture's User instances have null collections until reloaded
        reload();
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void noPlans(String prefName) {
        assertNull(defaultValue(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void noActivePlan_firstOwned(String prefName) {
        planRepo.save(new Plan(alice, "Beta"));
        Plan alpha = planRepo.save(new Plan(alice, "Alpha"));
        share(planRepo.save(new Plan(bob, "Aardvark")));

        assertEquals(ids(alpha), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void noActivePlan_firstShared(String prefName) {
        share(planRepo.save(new Plan(bob, "Beta")));
        Plan alpha = share(planRepo.save(new Plan(bob, "Alpha")));

        assertEquals(ids(alpha), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void activePlan_owned(String prefName) {
        planRepo.save(new Plan(alice, "Alpha"));
        Plan beta = planRepo.save(new Plan(alice, "Beta"));
        setActivePlan(null, beta.getId().toString());

        assertEquals(ids(beta), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void activePlan_shared(String prefName) {
        planRepo.save(new Plan(alice, "Alpha"));
        Plan beta = share(planRepo.save(new Plan(bob, "Beta")));
        setActivePlan(null, beta.getId().toString());

        assertEquals(ids(beta), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void activePlan_inaccessible(String prefName) {
        Plan alpha = planRepo.save(new Plan(alice, "Alpha"));
        Plan bobs = planRepo.save(new Plan(bob, "Bob's"));
        setActivePlan(null, bobs.getId().toString());

        assertEquals(ids(alpha), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void activePlan_unknown(String prefName) {
        Plan alpha = planRepo.save(new Plan(alice, "Alpha"));
        setActivePlan(null, UNKNOWN_PLAN_ID);

        assertEquals(ids(alpha), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void activePlan_notAnId(String prefName) {
        Plan alpha = planRepo.save(new Plan(alice, "Alpha"));
        setActivePlan(null, NOT_AN_ID);

        assertEquals(ids(alpha), defaultIds(prefName, null));
    }

    @ParameterizedTest
    @MethodSource("activePlanFollowers")
    void activePlan_deviceScoped(String prefName) {
        Plan alpha = planRepo.save(new Plan(alice, "Alpha"));
        Plan beta = planRepo.save(new Plan(alice, "Beta"));
        setActivePlan(DEVICE_KEY, beta.getId().toString());

        assertEquals(ids(beta), defaultIds(prefName, DEVICE_KEY));
        assertEquals(ids(alpha), defaultIds(prefName, null));
    }

    private Plan share(Plan bobsPlan) {
        bobsPlan.getAcl().setGrant(alice, AccessLevel.VIEW);
        return bobsPlan;
    }

    private void setActivePlan(String deviceKey, String value) {
        setUserPreference.set(alice,
                              Preference.PREF_ACTIVE_PLAN,
                              deviceKey,
                              value);
    }

    private List<String> ids(Plan... plans) {
        return Stream.of(plans)
                .map(p -> p.getId().toString())
                .toList();
    }

    private List<String> defaultIds(String prefName, String deviceKey) {
        try {
            return objectMapper.readValue(defaultValue(prefName, deviceKey),
                                          new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String defaultValue(String prefName, String deviceKey) {
        reload();
        UserDevice device = deviceKey == null
                ? null
                : ensureUserDevice.forWrite(alice, deviceKey);
        return defPref.build(alice,
                             preferenceRepo.getByName(prefName),
                             device)
                .getValue();
    }

    private void reload() {
        entityManager.flush();
        entityManager.clear();
        alice = userRepo.getByName("Alice");
        bob = userRepo.getByName("Bob");
    }

}
