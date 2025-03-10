package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class GetUserPreference {

    public Optional<UserPreference> find(User user,
                                         String prefName,
                                         UserDevice device) {
        return user.getPreferences()
                .stream()
                .filter(it -> it.answersTo(prefName))
                .filter(it -> Objects.equals(device, it.getDevice()))
                .findFirst();
    }

    public Optional<UserPreference> find(User user,
                                         Preference preference,
                                         UserDevice device) {
        return find(user, preference.getName(), device);
    }

    public UserPreference get(User user,
                              String prefName,
                              UserDevice device) {
        return find(user, prefName, device)
                .orElseThrow(() -> {
                    if (device == null) return new NoResultException(String.format(
                            "User '%s' has no global '%s' preference",
                            user.getId(),
                            prefName));
                    return new NoResultException(String.format(
                            "User '%s' has no '%s' preference for '%s'",
                            user.getId(),
                            prefName,
                            device.getId()));
                });
    }

    public UserPreference get(User user,
                              Preference preference,
                              UserDevice device) {
        return get(user, preference.getName(), device);
    }

}
