package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Friend;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface FriendRepository extends Repository<Friend, Long>, FriendSearchRepository {
}
