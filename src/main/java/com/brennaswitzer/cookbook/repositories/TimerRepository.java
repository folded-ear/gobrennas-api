package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Timer;
import com.brennaswitzer.cookbook.domain.User;

public interface TimerRepository extends BaseEntityRepository<Timer> {

    Iterable<Timer> findByAclOwnerOrderByCreatedAt(User owner);

}
