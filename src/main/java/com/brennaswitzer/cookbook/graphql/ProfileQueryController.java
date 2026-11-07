package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Friend;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.model.GqlSearch;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnection;
import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import com.brennaswitzer.cookbook.repositories.FriendRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.repositories.impl.FriendSearchRequest;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.ScratchUpload;
import graphql.relay.Connection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileQueryController {

    record ProfileQuery() {}

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    static class FriendSearch implements GqlSearch {

        String query;
        int first;
        OffsetConnectionCursor after;

    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private ScratchSpace scratchSpace;

    @QueryMapping
    ProfileQuery profile() {
        return new ProfileQuery();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    User me(ProfileQuery profileQ,
            @CurrentUser UserPrincipal userPrincipal) {
        return userRepository.getReferenceById(userPrincipal.getId());
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Iterable<User> friends(ProfileQuery profileQ,
                           @CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findByIdNot(userPrincipal.getId());
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Connection<Friend> searchFriends(ProfileQuery profileQ,
                                     @Argument FriendSearch search,
                                     @CurrentUser UserPrincipal userPrincipal) {
        return new OffsetConnection<>(friendRepository.search(
                FriendSearchRequest.builder()
                        .excludedUserId(userPrincipal.getId())
                        .query(search.getQuery())
                        .offset(search.getOffset())
                        .limit(search.getFirst())
                        .build()));
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    ScratchUpload scratchFile(ProfileQuery profileQ,
                              @Argument String contentType,
                              @Argument String originalFilename,
                              @CurrentUser UserPrincipal userPrincipal) {
        return scratchSpace.newScratchFile(
                userPrincipal,
                contentType,
                originalFilename);
    }

}
