package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithAliceBobEve
class PlanAuthorizationTest {

    private static final String ITEM_NAME = "OJ";
    private static final String BUCKET_NAME = "Monday";

    @Autowired
    private PlanService service;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = userRepo.getByName("Alice");
        bob = userRepo.getByName("Bob");
    }

    @Test
    void noGrant() {
        Plan plan = bobsPlan(null);
        PlanItem oj = item(plan);

        assertThrows(AccessDeniedException.class,
                     () -> service.getPlanById(plan.getId()));
        assertThrows(AccessDeniedException.class,
                     () -> service.getPlanItemById(oj.getId()));
        assertThrows(AccessDeniedException.class,
                     () -> service.getTreeDeltasById(plan.getId(), Instant.EPOCH));
        assertThrows(AccessDeniedException.class,
                     () -> service.duplicatePlan("mine now", plan.getId()));
    }

    @Test
    void viewGrant() {
        Plan plan = bobsPlan(AccessLevel.VIEW);
        PlanItem oj = item(plan);

        assertSame(plan, service.getPlanById(plan.getId()));
        assertSame(oj, service.getPlanItemById(oj.getId()));
        assertEquals("copy",
                     service.duplicatePlan("copy", plan.getId()).getName());
        assertThrows(AccessDeniedException.class,
                     () -> service.renameItem(oj.getId(), "orange juice"));
        assertThrows(AccessDeniedException.class,
                     () -> service.createItem(plan.getId(), null, "milk"));
        assertThrows(AccessDeniedException.class,
                     () -> service.setColor(plan.getId(), null));
        assertEquals(ITEM_NAME, oj.getName());
    }

    @Test
    void changeGrant() {
        Plan plan = bobsPlan(AccessLevel.CHANGE);
        PlanItem oj = item(plan);
        PlanItem milk = item(plan);

        service.renameItem(oj.getId(), "orange juice");
        service.mutateTree(List.of(milk.getId()), oj.getId(), null);

        assertEquals("orange juice", oj.getName());
        assertSame(oj, milk.getParent());
        assertThrows(AccessDeniedException.class,
                     () -> service.createBucket(plan.getId(), BUCKET_NAME, null));
        assertThrows(AccessDeniedException.class,
                     () -> service.setGrantOnPlan(plan.getId(),
                                                  alice.getId(),
                                                  AccessLevel.ADMINISTER));
    }

    @Test
    void administerGrant() {
        Plan plan = bobsPlan(AccessLevel.ADMINISTER);
        PlanBucket bucket = bucket(plan);

        service.updateBucket(plan.getId(), bucket.getId(), "Tuesday", null);

        assertEquals("Tuesday", bucket.getName());
    }

    @Test
    void mutateTreeAcrossPlans() {
        Plan mine = alicesPlan();
        PlanItem oj = item(mine);
        PlanItem bobsItem = item(bobsPlan(AccessLevel.VIEW));

        assertThrows(AccessDeniedException.class,
                     () -> service.mutateTree(List.of(bobsItem.getId()),
                                              oj.getId(),
                                              null));
    }

    @Test
    void bucketOnAnInaccessiblePlan() {
        Plan mine = alicesPlan();
        PlanBucket myBucket = bucket(mine);
        PlanBucket bobsBucket = bucket(bobsPlan(AccessLevel.VIEW));

        assertThrows(AccessDeniedException.class,
                     () -> service.updateBucket(mine.getId(),
                                                bobsBucket.getId(),
                                                "mine now",
                                                null));
        assertThrows(AccessDeniedException.class,
                     () -> service.moveBucket(mine.getId(),
                                              myBucket.getId(),
                                              bobsBucket.getId()));
        assertThrows(AccessDeniedException.class,
                     () -> service.deleteBucket(mine.getId(),
                                                bobsBucket.getId()));
        assertThrows(AccessDeniedException.class,
                     () -> service.deleteBuckets(mine.getId(),
                                                 List.of(bobsBucket.getId())));
        assertEquals(BUCKET_NAME, bobsBucket.getName());
    }

    @Test
    void bucketOnAnotherAccessiblePlan() {
        Plan mine = alicesPlan();
        PlanBucket otherBucket = bucket(alicesPlan());

        assertThrows(IllegalArgumentException.class,
                     () -> service.updateBucket(mine.getId(),
                                                otherBucket.getId(),
                                                "moved",
                                                null));
        assertThrows(IllegalArgumentException.class,
                     () -> service.deleteBucket(mine.getId(),
                                                otherBucket.getId()));
        assertEquals(BUCKET_NAME, otherBucket.getName());
    }

    @Test
    void missingItem() {
        assertThrows(EntityNotFoundException.class,
                     () -> service.renameItem(-1L, "nothing"));
    }

    private Plan alicesPlan() {
        Plan plan = new Plan(alice, "Alice's");
        entityManager.persist(plan);
        return plan;
    }

    private Plan bobsPlan(AccessLevel aliceLevel) {
        Plan plan = new Plan(bob, "Bob's");
        if (aliceLevel != null) {
            plan.getAcl().setGrant(alice, aliceLevel);
        }
        entityManager.persist(plan);
        return plan;
    }

    private PlanItem item(PlanItem parent) {
        PlanItem item = new PlanItem(ITEM_NAME).of(parent);
        entityManager.persist(item);
        return item;
    }

    private PlanBucket bucket(Plan plan) {
        PlanBucket bucket = new PlanBucket(plan, BUCKET_NAME, null);
        entityManager.persist(bucket);
        return bucket;
    }

}
