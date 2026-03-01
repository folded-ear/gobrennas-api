package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.Preference;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * I ensure a user has at least one plan, and return one such.
 */
@Component
public class EnsureUserHasAPlan {

    @Autowired
    protected PlanService planService;

    @Autowired
    protected PlanRepository planRepo;

    public Plan ensurePlan(User user) {
        Iterator<Plan> itr = planRepo.findByOwner(user).iterator();
        if (itr.hasNext()) return itr.next();
        Plan plan = planService.createPlan("My Week", user);
        user.getPreferences()
                .stream()
                .filter(p -> p.answersTo(Preference.PREF_ACTIVE_PLAN))
                .forEach(p -> p.setValue(plan.getId().toString()));
        return plan;
    }

}
