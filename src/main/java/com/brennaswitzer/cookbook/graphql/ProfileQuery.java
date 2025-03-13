package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.ScratchUpload;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileQuery {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScratchSpace scratchSpace;

    public User me(DataFetchingEnvironment env) {
        return userRepository.getReferenceById(PrincipalUtil.from(env).getId());
    }

    public Iterable<User> friends(DataFetchingEnvironment env) {
        return userRepository.findByIdNot(PrincipalUtil.from(env).getId());
    }

    public ScratchUpload scratchFile(String contentType,
                                     String originalFilename,
                                     DataFetchingEnvironment env) {
        return scratchSpace.newScratchFile(
                PrincipalUtil.from(env),
                contentType,
                originalFilename);
    }

}
