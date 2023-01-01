package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Timer;
import com.brennaswitzer.cookbook.services.timers.UpdateTimers;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class TimerMutation implements GraphQLMutationResolver {

    @Autowired
    private UpdateTimers update;

    public Timer createTimer(int duration) {
        return update.createTimer(duration);
    }

    public Timer pauseTimer(Long id) {
        return update.pauseTimer(id);
    }

    public Timer resumeTimer(Long id) {
        return update.resumeTimer(id);
    }

    public boolean deleteTimer(Long id) {
        return update.deleteTimer(id);
    }

}
