package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.ScratchUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileQueryController {

    record ProfileQuery() {}

    @Autowired
    private UserRepository userRepository;

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
