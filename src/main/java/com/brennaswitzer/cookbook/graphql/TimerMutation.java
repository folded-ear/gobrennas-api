package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Timer;
import com.brennaswitzer.cookbook.services.timers.UpdateTimers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class TimerMutation {

    @Autowired
    private UpdateTimers update;

    public Timer create(int duration) {
        return update.createTimer(duration);
    }

    public Timer pause(Long id) {
        return update.pauseTimer(id);
    }

    public Timer resume(Long id) {
        return update.resumeTimer(id);
    }

    public boolean delete(Long id) {
        return update.deleteTimer(id);
    }

}
