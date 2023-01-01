package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Timer;
import com.brennaswitzer.cookbook.services.timers.FetchTimers;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class TimerQuery implements GraphQLQueryResolver {

    @Autowired
    private FetchTimers fetch;

    public Iterable<Timer> getTimers() {
        return fetch.getTimersForUser();
    }

    public Timer getTimer(Long id) {
        return fetch.getTimerById(id);
    }

}
