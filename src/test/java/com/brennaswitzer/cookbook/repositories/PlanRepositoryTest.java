package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
public class PlanRepositoryTest {

    @Autowired
    private PlanRepository repo;

    @Autowired
    private UserRepository userRepo;

    private User alice, bob, eve;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
        bob = userRepo.getByName("Bob");
        eve = userRepo.getByName("Eve");
    }

    @Test
    public void createUpdateTimestamps() throws InterruptedException {
        assertFalse(repo.findByOwner(alice).iterator().hasNext());

        Plan groceries = new Plan(alice, "Groceries");
        assertNull(groceries.getCreatedAt());
        assertNull(groceries.getUpdatedAt());
        groceries = repo.save(groceries);

        assertNotNull(groceries.getCreatedAt());
        assertNotNull(groceries.getUpdatedAt());
        assertTrue(repo.findByOwner(alice).iterator().hasNext());

        Instant old = groceries.getUpdatedAt();
        assertEquals(old, groceries.getUpdatedAt());

        groceries.setName("Grocery List");
        Thread.sleep(123);
        repo.flush();

        assertNotEquals(old, groceries.getUpdatedAt());
    }

    @Test
    public void findAccessiblePlans() {
        Iterable<Plan> lists = repo.findAccessiblePlans(alice.getId());
        Iterator<Plan> itr = lists.iterator();
        assertFalse(itr.hasNext());

        Plan alicesList = repo.save(new Plan(alice, "Alice's List"));
        Plan bobsList = repo.save(new Plan(bob, "Bob's List"));

        lists = repo.findAccessiblePlans(alice.getId());
        itr = lists.iterator();
        assertEquals("Alice's List", itr.next().getName());
        assertFalse(itr.hasNext());

        alicesList.getAcl().setGrant(bob, AccessLevel.CHANGE);
        alicesList.getAcl().setGrant(eve, AccessLevel.CHANGE);
        bobsList.getAcl().setGrant(alice, AccessLevel.CHANGE);

        lists = repo.findAccessiblePlans(alice.getId());
        itr = lists.iterator();
        assertEquals("Alice's List", itr.next().getName());
        assertEquals("Bob's List", itr.next().getName());
        assertFalse(itr.hasNext());
    }

}
