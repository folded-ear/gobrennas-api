package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.PreferenceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.function.Function;

@Service
@Transactional
public class DefaultUserPreference {

    @Autowired
    private EnsureUserDevice ensureUserDevice;

    @Autowired
    private GetUserPreference getUserPreference;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PreferenceRepository preferenceRepo;
    @Autowired
    private PlanRepository planRepo;

    public UserPreference build(UserPrincipal userPrincipal, String name, String deviceKey) {
        return build(userRepo.getReferenceById(userPrincipal.getId()),
                     preferenceRepo.getByName(name),
                     deviceKey);
    }

    public UserPreference build(UserPrincipal userPrincipal, Preference preference, String deviceKey) {
        return build(userRepo.getReferenceById(userPrincipal.getId()),
                     preference,
                     deviceKey);
    }

    public UserPreference build(User user, Preference preference, String deviceKey) {
        return build(user,
                     preference,
                     ensureUserDevice.ensure(user, deviceKey));
    }

    public UserPreference build(User user, Preference preference, UserDevice device) {
        UserPreference pref = new UserPreference();
        pref.setUser(user);
        pref.setPreference(preference);
        pref.setDevice(device);
        pref.setValue(switch (preference.getName()) {
            case Preference.PREF_ACTIVE_PLAN -> getDefaultPlanId(user);
            case Preference.PREF_ACTIVE_SHOPPING_PLANS -> {
                // use the active plan, if one exists
                var id = getUserPreference.find(user, preference, device)
                        .map(UserPreference::getValue)
                        .orElseGet(() -> getDefaultPlanId(user));
                if (id == null) yield null;
                try {
                    yield objectMapper.writeValueAsString(Set.of(id));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> preference.getDefaultValue();
        });
        return pref;
    }

    public Function<Preference, UserPreference> factory(
            User user,
            String deviceKey) {
        return factory(user, ensureUserDevice.ensure(user, deviceKey));
    }

    public Function<Preference, UserPreference> factory(
            User user,
            UserDevice device) {
        return p -> build(user, p, device);
    }

    private String getDefaultPlanId(User user) {
        var plans = planRepo.findByOwner(user)
                .iterator();
        if (plans.hasNext()) return plans.next().getId().toString();
        plans = planRepo.findAccessiblePlans(user.getId())
                .iterator();
        if (plans.hasNext()) return plans.next().getId().toString();
        return null;
    }

}
