package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Friend;
import org.springframework.data.repository.Repository;

public interface FriendRepository extends Repository<Friend, Long>, FriendSearchRepository {
}
