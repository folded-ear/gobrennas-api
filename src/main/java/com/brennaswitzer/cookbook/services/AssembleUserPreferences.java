package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssembleUserPreferences {

    @Autowired
    private EnsureUserDevice ensureUserDevice;

    @Autowired
    private DefaultUserPreference defaultUserPreference;

    @Autowired
    private PreferenceRepository preferenceRepo;

    public Collection<UserPreference> assemble(User user,
                                               String deviceKey) {
        return assemble(user, ensureUserDevice.forRead(user, deviceKey));
    }

    public Collection<UserPreference> assemble(User user,
                                               UserDevice device) {
        Map<Preference, UserPreference> byPref = user.getPreferences()
                .stream()
                .filter(p -> Objects.equals(device, p.getDevice()))
                .collect(Collectors.toMap(UserPreference::getPreference,
                                          Function.identity()));
        var factory = defaultUserPreference.factory(user, device);
        return preferenceRepo.findAll()
                .stream()
                .map(p -> byPref.computeIfAbsent(p, factory))
                .toList();
    }

}
