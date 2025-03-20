package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Preference;
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
public class ClearUserPreference {

    @Autowired
    private EnsureUserDevice ensureUserDevice;

    @Autowired
    private GetUserPreference getUserPreference;

    @Autowired
    private DefaultUserPreference defaultUserPreference;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PreferenceRepository preferenceRepo;
    @Autowired
    private UserPreferenceRepository userPrefRepo;

    public UserPreference clear(UserPrincipal userPrincipal,
                                String prefName,
                                String deviceKey) {
        return clear(userRepo.getReferenceById(userPrincipal.getId()),
                     prefName,
                     deviceKey);
    }

    public UserPreference clear(User user,
                                String prefName,
                                String deviceKey) {
        return clear(user,
                     prefName,
                     ensureUserDevice.forWrite(user, deviceKey));
    }

    public UserPreference clear(User user,
                                String prefName,
                                UserDevice device) {
        Preference preference = getUserPreference.find(user, prefName, device)
                .map(it -> {
                    Preference p = it.getPreference();
                    if (it.getDevice() != null) {
                        it.getDevice().getPreferences().remove(it);
                        it.setDevice(null);
                    }
                    it.getUser().getPreferences().remove(it);
                    it.setUser(null);
                    userPrefRepo.delete(it);
                    return p;
                })
                .orElseGet(() -> preferenceRepo.getByName(prefName));
        return defaultUserPreference.build(user, preference, device);
    }

}
