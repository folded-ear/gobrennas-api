package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Friend;
import com.brennaswitzer.cookbook.repositories.impl.FriendSearchRequest;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithAliceBobEve
class FriendSearchRepositoryImplTest {

    @Autowired
    private FriendRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Test
    void blankQueryFindsEveryoneElse() {
        assertEquals(List.of("Bob", "Eve"), search(" "));
    }

    @Test
    void everyTermMustMatchNameOrEmail() {
        assertEquals(List.of("Bob"), search(" BOB  gobrennas "));
        assertEquals(List.of(), search("  bob   eve@ "));
    }

    @Test
    void likeMetacharactersAreLiteral() {
        assertEquals(List.of(), search("%"));
        assertEquals(List.of(), search("b_b"));
    }

    private List<String> search(String query) {
        return repo.search(FriendSearchRequest.builder()
                                   .excludedUserId(principalAccess.getId())
                                   .query(query)
                                   .build())
                .getContent()
                .stream()
                .map(Friend::getName)
                .toList();
    }

}
