package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class EnsureUserHasAPlanDbTest {

    @Autowired
    private EnsureUserHasAPlan ensureUserHasAPlan;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PlanRepository planRepo;

    private User alice;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
    }

    @Test
    void everyoneNeedsAPlan() {
        assertEquals(0,
                     planRepo.count());
        List<User> users = userRepo.findUsersWithoutAPlan().toList();
        assertTrue(users.contains(alice),
                   "should have contained alice, but didn't");

        ensureUserHasAPlan.ensureEveryUserHasAPlan();

        assertEquals(List.of(),
                     userRepo.findUsersWithoutAPlan().toList());
        assertEquals(3,
                     planRepo.count());
    }

    @Test
    void findUsersWithoutAPlan_notNeeded() {
        ensureUserHasAPlan.ensurePlan(alice);
        assertEquals(1,
                     planRepo.count());
        List<User> users = userRepo.findUsersWithoutAPlan().toList();
        assertFalse(users.contains(alice),
                    "should have contained alice, but didn't");

        ensureUserHasAPlan.ensureEveryUserHasAPlan();

        assertEquals(List.of(),
                     userRepo.findUsersWithoutAPlan().toList());
        assertEquals(3,
                     planRepo.count());
    }

}
