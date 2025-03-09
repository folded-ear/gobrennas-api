package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PreferenceRepository;
import com.brennaswitzer.cookbook.repositories.UserPreferenceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SetUserPreference {

    @Autowired
    private EnsureUserDevice ensureUserDevice;

    @Autowired
    private GetUserPreference getUserPreference;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PreferenceRepository preferenceRepo;
    @Autowired
    private UserPreferenceRepository userPrefRepo;

    public UserPreference set(UserPrincipal userPrincipal,
                              String prefName,
                              String deviceKey,
                              String value) {
        return set(userRepo.getReferenceById(userPrincipal.getId()),
                   prefName,
                   deviceKey,
                   value);
    }

    public UserPreference set(User user,
                              String prefName,
                              String deviceKey,
                              String value) {
        return set(user,
                   prefName,
                   ensureUserDevice.ensure(user, deviceKey),
                   value);
    }

    public UserPreference set(User user,
                              String prefName,
                              UserDevice device,
                              String value) {
        UserPreference pref = getUserPreference.find(user, prefName, device)
                .orElseGet(() -> {
                    UserPreference p = new UserPreference();
                    p.setUser(user);
                    p.setPreference(preferenceRepo.getByName(prefName));
                    p.setDevice(device);
                    return p;
                });
        pref.setValue(value);
        return userPrefRepo.save(pref);
    }

}
