package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
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
        var device = userDeviceRepo.findByUserIdAndKey(user.getId(), key)
                .orElseGet(() -> {
                    var d = new UserDevice();
                    User u = userRepo.getReferenceById(user.getId());
                    d.setUser(u);
                    u.getDevices().add(d);
                    d.setKey(key);
                    d.setName("New Device (" + LocalDate.now() + ')');
                    return d;
                });
        device.markEnsured();
        return userDeviceRepo.save(device);
    }

}
