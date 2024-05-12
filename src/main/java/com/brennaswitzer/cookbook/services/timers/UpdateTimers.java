package com.brennaswitzer.cookbook.services.timers;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Timer;
import com.brennaswitzer.cookbook.repositories.TimerRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateTimers {

    @Autowired
    private TimerRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Timer createTimer(int duration) {
        val t = new Timer();
        t.setOwner(principalAccess.getUser());
        t.setDuration(duration);
        return repo.save(t);
    }

    public Timer pauseTimer(Long id) {
        val t = repo.getReferenceById(id);
        t.ensurePermitted(principalAccess.getUser(), AccessLevel.CHANGE);
        t.pause();
        return t;
    }

    public Timer resumeTimer(Long id) {
        val t = repo.getReferenceById(id);
        t.ensurePermitted(principalAccess.getUser(), AccessLevel.CHANGE);
        t.resume();
        return t;
    }

    public Timer addTime(Long id, int duration) {
        val t = repo.getReferenceById(id);
        t.ensurePermitted(principalAccess.getUser(), AccessLevel.CHANGE);
        t.addExtraTime(duration);
        return t;
    }

    public Timer deleteTimer(Long id) {
        val t = repo.getReferenceById(id);
        t.ensurePermitted(principalAccess.getUser(), AccessLevel.CHANGE);
        repo.delete(t);
        return t;
    }

}
