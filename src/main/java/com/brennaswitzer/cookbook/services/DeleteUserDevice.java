package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteUserDevice {

    @Autowired
    private GetUserDevice getUserDevice;

    @Autowired
    private UserDeviceRepository userDeviceRepo;

    public Deletion delete(UserPrincipal owner,
                           Long deviceId) {
        return delete(owner.getId(), deviceId);
    }

    public Deletion delete(User owner,
                           Long deviceId) {
        return delete(owner.getId(), deviceId);
    }

    private Deletion delete(Long ownerId,
                            Long id) {
        UserDevice d = getUserDevice.get(ownerId, id);
        userDeviceRepo.delete(d);
        return Deletion.of(d);
    }

}
