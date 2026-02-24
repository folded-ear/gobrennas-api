package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class EnsureUserDevice {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserDeviceRepository userDeviceRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserDevice forRead(Identified user, String key) {
        if (key == null) return null;
        return loadEnsureAndSave(user, key);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserDevice forWrite(User user, String key) {
        if (key == null) return null;
        for (var d : user.getDevices()) {
            if (key.equals(d.getKey())) {
                d.markEnsured();
                return d;
            }
        }
        return loadEnsureAndSave(user, key);
    }

    public UserDevice loadEnsureAndSave(Identified user, String key) {
        Optional<UserDevice> optDevice = userDeviceRepo.findByUserIdAndKey(user.getId(), key);
        UserDevice device;
        if (optDevice.isPresent()) {
            device = optDevice.get();
            if (shouldSkipEnsure(device)) {
                log.info("Skip ensuring device '{}' for user '{}'.",
                         key,
                         user.getId());
                return device;
            }
        } else {
            device = new UserDevice();
            User u = userRepo.getReferenceById(user.getId());
            device.setUser(u);
            u.getDevices().add(device);
            device.setKey(key);
            device.setName("New Device (" + LocalDate.now() + ')');
        }
        device.markEnsured();
        return userDeviceRepo.save(device);
    }

    @VisibleForTesting
    boolean shouldSkipEnsure(UserDevice device) {
        // Skip it 90% of the time, if it's already been ensured today.
        return Math.random() < 0.9
               && device.getLastEnsuredAt().isAfter(
                Instant.now().minusSeconds(86400));
    }

}
