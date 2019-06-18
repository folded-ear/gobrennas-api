package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import static com.brennaswitzer.cookbook.util.UserTestUtils.createUser;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class AliceBobEveContext implements WithSecurityContextFactory<WithAliceBobEve> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public SecurityContext createSecurityContext(WithAliceBobEve annotation) {
        User alice = userRepository.save(createUser("Alice"));
        userRepository.save(createUser("Bob"));
        userRepository.save(createUser("Eve"));
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        if (annotation.authentication()) {
            UserPrincipal principal = UserPrincipal.create(alice);
            Authentication auth =
                    new UsernamePasswordAuthenticationToken(principal, "<HIDDEN>", principal.getAuthorities());
            context.setAuthentication(auth);
        }
        return context;
    }

}
