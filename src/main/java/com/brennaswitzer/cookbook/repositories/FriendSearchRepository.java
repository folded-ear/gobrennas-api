package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Friend;
import com.brennaswitzer.cookbook.repositories.impl.FriendSearchRequest;

public interface FriendSearchRepository {

    SearchResponse<Friend> search(FriendSearchRequest request);

}
