package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends BaseEntityRepository<UserDevice> {

    Optional<UserDevice> findByUserAndKey(User user,
                                          String deviceKey);

}
