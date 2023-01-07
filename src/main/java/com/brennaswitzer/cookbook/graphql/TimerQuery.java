package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Timer;
import com.brennaswitzer.cookbook.services.timers.FetchTimers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class TimerQuery {

    @Autowired
    private FetchTimers fetch;

    public Iterable<Timer> all() {
        return fetch.getTimersForUser();
    }

    public Timer byId(Long id) {
        return fetch.getTimerById(id);
    }

}
