package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.TextractJobStatus;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class DeleteOldTextractJobsTest {

    private static final TextractJob.Line LINE = new TextractJob.Line(
            "one cup flour",
            new TextractJob.Box(0.1, 0.2, 0.3, 0.4));

    private static final String SET_UPDATED_AT = """
            update TextractJob
            set updatedAt = ?1
            where id = ?2""";

    @Autowired AppProperties appProperties;
    @Autowired TextractJobRepository repo;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private final FakeStorage storage = new FakeStorage();

    private DeleteOldTextractJobs deleteOldJobs;

    private Instant tomorrow;

    @BeforeEach
    void setUp() {
        deleteOldJobs = new DeleteOldTextractJobs(
                appProperties,
                repo,
                new DeleteTextractJob(repo, storage));
        tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);
    }

    @ParameterizedTest
    @EnumSource(TextractJobStatus.class)
    void staleJobIsDeletedInAnyStatus(TextractJobStatus status) {
        TextractJob job = job("photo");
        switch (status) {
            case PENDING -> {}
            case READY -> job.succeed(Set.of(LINE));
            case ERROR -> job.fail("Rate exceeded", true);
        }
        em.flush();
        assertEquals(status, job.getStatus());

        deleteOldJobs.deleteJobsUpdatedBefore(tomorrow);

        assertFalse(exists(job));
        assertFalse(storage.holds("photo"));
    }

    @Test
    void unremovablePhotoKeepsItsJobAndSparesTheRest() {
        TextractJob stuck = job("stuck");
        TextractJob loose = job("loose");
        storage.refuseToRemove("stuck");

        assertDoesNotThrow(
                () -> deleteOldJobs.deleteJobsUpdatedBefore(tomorrow));

        assertTrue(exists(stuck));
        assertTrue(storage.holds("stuck"));
        assertFalse(exists(loose));
        assertFalse(storage.holds("loose"));
    }

    @Test
    void scheduledSweepDeletesAJobIdlePastTheConfiguredDays() {
        TextractJob job = job("photo");
        idleFor(job, appProperties.getDaysTextractJobIdle() + 1);

        deleteOldJobs.deleteOldJobs();

        assertFalse(exists(job));
        assertFalse(storage.holds("photo"));
    }

    @Test
    void scheduledSweepKeepsAJobIdleShortOfTheConfiguredDays() {
        TextractJob job = job("photo");
        idleFor(job, appProperties.getDaysTextractJobIdle() - 1);

        deleteOldJobs.deleteOldJobs();

        assertTrue(exists(job));
        assertTrue(storage.holds("photo"));
    }

    private TextractJob job(String objectKey) {
        TextractJob job = new TextractJob();
        job.setOwner(principal.getUser());
        job.setPhoto(new S3File(objectKey, "image/jpeg", 1L));
        em.persist(job);
        storage.put(objectKey);
        return job;
    }

    /**
     * I make the job look last updated the passed number of days ago.
     */
    private void idleFor(TextractJob job, int days) {
        em.flush();
        em.createQuery(SET_UPDATED_AT)
                .setParameter(1, Instant.now().minus(days, ChronoUnit.DAYS))
                .setParameter(2, job.getId())
                .executeUpdate();
        em.clear();
    }

    private boolean exists(TextractJob job) {
        em.flush();
        em.clear();
        return repo.existsById(job.getId());
    }

}
