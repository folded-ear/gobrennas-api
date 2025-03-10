package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

@Service
@Transactional
public class EnsureUserDevice {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserDeviceRepository userDeviceRepo;

    private TransactionTemplate txTmpl;

    @Autowired
    @VisibleForTesting
    void setTransactionManager(PlatformTransactionManager transactionManager) {
        txTmpl = new TransactionTemplate(transactionManager);
        txTmpl.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public UserDevice ensure(UserPrincipal userPrincipal, String key) {
        if (key == null) return null;
        return ensure(userRepo.getReferenceById(userPrincipal.getId()),
                      key);
    }

    public UserDevice ensure(User user, String key) {
        if (key == null) return null;
        for (var d : user.getDevices())
            if (key.equals(d.getKey()))
                return d;
        return userDeviceRepo.findByUserAndKey(user, key)
                .orElseGet(() -> txTmpl.execute(tx -> {
                    UserDevice device = new UserDevice();
                    // reload the user in the inner transaction
                    User u = userRepo.getReferenceById(user.getId());
                    device.setUser(u);
                    u.getDevices().add(device);
                    device.setKey(key);
                    device.setName("New Device (" + LocalDate.now() + ')');
                    return userDeviceRepo.save(device);
                }));
    }

}
