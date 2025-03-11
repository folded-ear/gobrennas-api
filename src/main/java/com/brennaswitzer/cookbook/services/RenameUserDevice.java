package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RenameUserDevice {

    @Autowired
    private GetUserDevice getUserDevice;

    @Autowired
    private UserDeviceRepository userDeviceRepo;

    public UserDevice rename(UserPrincipal owner,
                             Long deviceId,
                             String newName) {
        return rename(owner.getId(), deviceId, newName);
    }

    public UserDevice rename(User owner,
                             Long deviceId,
                             String newName) {
        return rename(owner.getId(), deviceId, newName);
    }

    private UserDevice rename(Long ownerId,
                              Long deviceId,
                              String newName) {
        UserDevice d = getUserDevice.get(ownerId, deviceId);
        d.setName(newName);
        return userDeviceRepo.save(d);
    }

}
