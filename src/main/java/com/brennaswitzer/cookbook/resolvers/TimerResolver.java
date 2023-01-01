package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Timer;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
public class TimerResolver implements GraphQLResolver<Timer> {

    public List<AccessControlEntry> getGrants(Timer timer) {
        return AclHelpers.getGrants(timer);
    }
}
