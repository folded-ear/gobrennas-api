package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.ScratchUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileQuery {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScratchSpace scratchSpace;

    @SchemaMapping(typeName = "ProfileQuery")
    @PreAuthorize("hasRole('USER')")
    public User me(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userRepository.getReferenceById(userPrincipal.getId());
    }

    @SchemaMapping(typeName = "ProfileQuery")
    @PreAuthorize("hasRole('USER')")
    public Iterable<User> friends(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userRepository.findByIdNot(userPrincipal.getId());
    }

    @SchemaMapping(typeName = "ProfileQuery")
    @PreAuthorize("hasRole('USER')")
    public ScratchUpload scratchFile(@Argument String contentType,
                                     @Argument String originalFilename,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return scratchSpace.newScratchFile(
                userPrincipal,
                contentType,
                originalFilename);
    }

}
