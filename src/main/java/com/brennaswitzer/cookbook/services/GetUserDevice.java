package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GetUserDevice {

    @Autowired
    private UserDeviceRepository userDeviceRepo;

    public UserDevice get(UserPrincipal owner,
                          Long deviceId) {
        return get(owner.getId(), deviceId);
    }

    public UserDevice get(User owner,
                          Long deviceId) {
        return get(owner.getId(), deviceId);
    }

    UserDevice get(Long ownerId,
                   Long deviceId) {
        UserDevice d = userDeviceRepo.getReferenceById(deviceId);
        if (!ownerId.equals(d.getUser().getId())) {
            throw new EntityNotFoundException(String.format(
                    "No device '%s' found",
                    deviceId));
        }
        return d;
    }

}
