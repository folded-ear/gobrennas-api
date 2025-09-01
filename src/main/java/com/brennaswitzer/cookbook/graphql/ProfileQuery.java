package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.ScratchUpload;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileQuery {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScratchSpace scratchSpace;

    @SchemaMapping(typeName = "ProfileQuery")
    public User me(DataFetchingEnvironment env) {
        return userRepository.getReferenceById(PrincipalUtil.from(env).getId());
    }

    @SchemaMapping(typeName = "ProfileQuery")
    public Iterable<User> friends(DataFetchingEnvironment env) {
        return userRepository.findByIdNot(PrincipalUtil.from(env).getId());
    }

    @SchemaMapping(typeName = "ProfileQuery")
    public ScratchUpload scratchFile(@Argument String contentType,
                                     @Argument String originalFilename,
                                     DataFetchingEnvironment env) {
        return scratchSpace.newScratchFile(
                PrincipalUtil.from(env),
                contentType,
                originalFilename);
    }

}
