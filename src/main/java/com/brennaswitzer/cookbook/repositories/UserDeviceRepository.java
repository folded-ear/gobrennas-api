package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.UserDevice;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends BaseEntityRepository<UserDevice> {

    Optional<UserDevice> findByUserIdAndKey(Long userId,
                                            String deviceKey);

}
