package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class DeleteOldBucketsDbTest {

    @Autowired
    private DeleteOldBuckets deleteOldBuckets;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlanBucketRepository bucketRepo;

    @Autowired
    private PlanItemRepository itemRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private Plan plan;
    private LocalDate cutoff;

    @BeforeEach
    void setUp() {
        plan = planRepo.save(new Plan(userRepo.getByName("Alice"), "groceries"));
        cutoff = LocalDate.now()
                .minusDays(appProperties.getDaysPastBucketDate());
    }

    private PlanBucket bucket(Plan plan, String name, LocalDate date) {
        return bucketRepo.save(new PlanBucket(plan, name, date));
    }

    private PlanBucket bucket(String name, LocalDate date) {
        return bucket(plan, name, date);
    }

    private PlanItem item(PlanItem parent, String name, PlanBucket bucket) {
        PlanItem it = new PlanItem(name).of(parent);
        it.setBucket(bucket);
        return itemRepo.save(it);
    }

    private void runJob() {
        entityManager.flush();
        entityManager.clear();
        deleteOldBuckets.deleteOldBuckets();
        entityManager.flush();
        entityManager.clear();
    }

    private boolean exists(PlanBucket bucket) {
        return bucketRepo.existsById(bucket.getId());
    }

    @Test
    void emptyBucketBeforeCutoffIsDeleted() {
        PlanBucket b = bucket("b", cutoff.minusDays(1));

        runJob();

        assertFalse(exists(b));
    }

    @Test
    void emptyBucketOnCutoffIsDeleted() {
        PlanBucket b = bucket("b", cutoff);

        runJob();

        assertFalse(exists(b));
    }

    @Test
    void emptyBucketAfterCutoffIsKept() {
        PlanBucket b = bucket("b", cutoff.plusDays(1));

        runJob();

        assertTrue(exists(b));
    }

    @Test
    void undatedBucketIsKept() {
        PlanBucket b = bucket("b", null);

        runJob();

        assertTrue(exists(b));
    }

    @Test
    void oldBucketWithActiveItemIsKept() {
        PlanBucket b = bucket("b", cutoff.minusDays(1));
        item(plan, "a", b);
        item(plan, "t", b).moveToTrash();

        runJob();

        assertTrue(exists(b));
    }

    @Test
    void oldBucketWithOnlyTrashedItemIsDeleted() {
        PlanBucket b = bucket("b", cutoff.minusDays(1));
        PlanItem t = item(plan, "t", b);
        t.moveToTrash();

        runJob();

        assertFalse(exists(b));
        assertNull(itemRepo.getReferenceById(t.getId()).getBucket());
    }

    @Test
    void oldBucketWithOnlyImplicitlyTrashedItemIsDeleted() {
        PlanBucket b = bucket("b", cutoff.minusDays(1));
        PlanItem parent = item(plan, "parent", null);
        PlanItem child = item(parent, "child", b);
        parent.moveToTrash();

        runJob();

        assertFalse(exists(b));
        assertNull(itemRepo.getReferenceById(child.getId()).getBucket());
    }

    @Test
    void deletingMarksOnlyThatPlanDirty() {
        Plan other = planRepo.save(new Plan(userRepo.getByName("Bob"), "stuff"));
        bucket("old", cutoff.minusDays(1));
        bucket(other, "new", cutoff.plusDays(1));
        entityManager.flush();
        entityManager.clear();
        Plan planBefore = planRepo.getReferenceById(plan.getId());
        var planUpdatedAt = planBefore.getUpdatedAt();
        var planModCount = planBefore.getModCount();
        Plan otherBefore = planRepo.getReferenceById(other.getId());
        var otherUpdatedAt = otherBefore.getUpdatedAt();
        var otherModCount = otherBefore.getModCount();

        runJob();

        Plan planAfter = planRepo.getReferenceById(plan.getId());
        assertTrue(planAfter.getUpdatedAt().isAfter(planUpdatedAt));
        assertEquals(planModCount + 1, planAfter.getModCount());
        Plan otherAfter = planRepo.getReferenceById(other.getId());
        assertEquals(otherUpdatedAt, otherAfter.getUpdatedAt());
        assertEquals(otherModCount, otherAfter.getModCount());
    }

}
