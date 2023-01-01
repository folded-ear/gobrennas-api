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
public class FetchTimers {

    @Autowired
    private TimerRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Iterable<Timer> getTimersForUser() {
        // todo: use the ACL, not just ownership
        return repo.findByAclOwnerOrderByCreatedAt(principalAccess.getUser());
    }

    public Timer getTimerById(Long id) {
        val timer = repo.getReferenceById(id);
        timer.ensurePermitted(principalAccess.getUser(), AccessLevel.VIEW);
        return timer;
    }

}
