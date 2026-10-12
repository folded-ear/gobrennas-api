package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.TextractJobStatus;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
public class TextractJobRepositoryTest {

    @Autowired
    private TextractJobRepository repo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private User alice;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
    }

    @Test
    public void findByOwner() {
        assertEquals(0, repo.findAllByOwnerOrderByCreatedAtDesc(alice).size());

        TextractJob job = new TextractJob();
        job.setOwner(alice);
        S3File p = new S3File("yesterday", "blerg", 7L);
        job.setPhoto(p);
        entityManager.persist(job);
        entityManager.flush();
        job.setCreatedAt(Instant.now().minusSeconds(43200));

        job = new TextractJob();
        job.setOwner(alice);
        p = new S3File("today", "mlerg", 4L);
        job.setPhoto(p);
        job.succeed(Set.of());
        entityManager.persist(job);
        entityManager.flush();
        entityManager.clear();

        List<TextractJob> jobs = repo.findAllByOwnerOrderByCreatedAtDesc(alice);
        assertEquals(2, jobs.size());
        job = jobs.get(0);
        assertEquals("today", job.getPhoto().getObjectKey());
        assertTrue(job.isReady());
        job = jobs.get(1);
        assertEquals("yesterday", job.getPhoto().getObjectKey());
        assertFalse(job.isReady());
    }

    @Test
    public void newJobIsPending() {
        TextractJob job = reload(job());

        assertEquals(TextractJobStatus.PENDING, job.getStatus());
        assertFalse(job.isReady());
        assertNull(job.getErrorMessage());
        assertFalse(job.isRetryable());
    }

    @Test
    public void succeededJobIsReady() {
        TextractJob job = job();
        var line = new TextractJob.Line(
                "one cup flour",
                new TextractJob.Box(0.1, 0.2, 0.3, 0.4));
        job.succeed(Set.of(line));
        job = reload(job);

        assertEquals(TextractJobStatus.READY, job.getStatus());
        assertTrue(job.isReady());
        assertEquals(Set.of(line), job.getLines());
        assertNull(job.getErrorMessage());
        assertFalse(job.isRetryable());
    }

    @Test
    public void failedJobIsError() {
        TextractJob job = job();
        job.fail("Rate exceeded", true);
        job = reload(job);

        assertEquals(TextractJobStatus.ERROR, job.getStatus());
        assertFalse(job.isReady());
        assertEquals("Rate exceeded", job.getErrorMessage());
        assertTrue(job.isRetryable());
    }

    @Test
    public void retriedJobIsPending() {
        TextractJob job = job();
        job.fail("Rate exceeded", true);
        job = reload(job);
        job.retry();
        job = reload(job);

        assertEquals(TextractJobStatus.PENDING, job.getStatus());
        assertFalse(job.isReady());
        assertNull(job.getErrorMessage());
        assertFalse(job.isRetryable());
    }

    private TextractJob job() {
        TextractJob job = new TextractJob();
        job.setOwner(alice);
        job.setPhoto(new S3File("photo", "image/jpeg", 1L));
        entityManager.persist(job);
        return job;
    }

    private TextractJob reload(TextractJob job) {
        entityManager.flush();
        entityManager.clear();
        return repo.findById(job.getId()).orElseThrow();
    }

}
