package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileQuery {

    @Autowired
    private UserRepository userRepository;

    public User me(DataFetchingEnvironment env) {
        return userRepository.getReferenceById(PrincipalUtil.from(env).getId());
    }

    public Iterable<User> friends(DataFetchingEnvironment env) {
        return userRepository.findByIdNot(PrincipalUtil.from(env).getId());
    }

}
