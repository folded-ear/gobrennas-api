package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Iterator;

/**
 * I ensure a user has at least one plan, and return one such.
 */
@Component
@Transactional
public class EnsureUserHasAPlan {

    @Autowired
    @Lazy // to break the cycle in the bean graph
    private PlanService planService;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private UserRepository userRepo;

    public Plan ensurePlan(User user) {
        Iterator<Plan> itr = planRepo.findByOwner(user).iterator();
        if (itr.hasNext()) return itr.next();
        Plan plan = planService.createPlan("My Week", user);
        Collection<UserPreference> prefs = user.getPreferences();
        if (prefs != null) {
            prefs.stream()
                    .filter(p -> p.answersTo(Preference.PREF_ACTIVE_PLAN))
                    .forEach(p -> p.setValue(plan.getId().toString()));
        }
        return plan;
    }

    public void ensureEveryUserHasAPlan() {
        userRepo.findUsersWithoutAPlan()
                .forEach(this::ensurePlan);
    }

}
