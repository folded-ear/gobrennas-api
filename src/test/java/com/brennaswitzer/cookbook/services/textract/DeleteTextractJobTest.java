package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class DeleteTextractJobTest {

    private static final String OBJECT_KEY = "textract/photo.jpg";

    private static final TextractJob.Line LINE = new TextractJob.Line(
            "one cup flour",
            new TextractJob.Box(0.1, 0.2, 0.3, 0.4));

    private static final String COUNT_LINES = """
            select count(*)
            from textract_job_lines
            where textract_job_id = ?1""";

    @Autowired TextractJobRepository repo;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private final FakeStorage storage = new FakeStorage();

    private DeleteTextractJob deleteJob;

    private TextractJob job;

    @BeforeEach
    void setUp() {
        deleteJob = new DeleteTextractJob(repo, storage);
        job = new TextractJob();
        job.setOwner(principal.getUser());
        job.setPhoto(new S3File(OBJECT_KEY, "image/jpeg", 1L));
        em.persist(job);
        storage.put(OBJECT_KEY);
        job = stored();
    }

    @Test
    void deleteRemovesTheJobAndItsPhoto() {
        Optional<TextractJob> deleted = deleteJob.delete(job.getId());

        assertEquals(Optional.of(job), deleted);
        assertFalse(exists());
        assertFalse(storage.holds(OBJECT_KEY));
    }

    @Test
    void deleteRemovesTheJobsLines() {
        job.succeed(Set.of(LINE));
        em.flush();
        assertEquals(1L, countLines());

        deleteJob.delete(job.getId());

        assertFalse(exists());
        assertEquals(0L, countLines());
    }

    @Test
    void deleteOfAnUnknownJobDeletesNothing() {
        Optional<TextractJob> deleted = deleteJob.delete(-1L);

        assertTrue(deleted.isEmpty());
        assertTrue(exists());
        assertTrue(storage.holds(OBJECT_KEY));
    }

    @Test
    void deleteKeepsAJobWhosePhotoCannotBeRemoved() {
        storage.refuseToRemove(OBJECT_KEY);

        assertThrows(IllegalStateException.class,
                     () -> deleteJob.delete(job.getId()));

        assertTrue(exists());
        assertTrue(storage.holds(OBJECT_KEY));
    }

    @Test
    void deleteIfStaleRemovesAJobUpdatedBeforeTheCutoff() {
        Instant cutoff = job.getUpdatedAt().plusMillis(1);

        Optional<TextractJob> deleted = deleteJob.deleteIfStale(
                job.getId(),
                cutoff);

        assertEquals(Optional.of(job), deleted);
        assertFalse(exists());
        assertFalse(storage.holds(OBJECT_KEY));
    }

    @Test
    void deleteIfStaleKeepsAJobUpdatedAtTheCutoff() {
        Instant cutoff = job.getUpdatedAt();

        Optional<TextractJob> deleted = deleteJob.deleteIfStale(
                job.getId(),
                cutoff);

        assertTrue(deleted.isEmpty());
        assertTrue(exists());
        assertTrue(storage.holds(OBJECT_KEY));
    }

    @Test
    void deleteIfStaleKeepsAJobWhosePhotoCannotBeRemoved() {
        storage.refuseToRemove(OBJECT_KEY);
        Instant cutoff = job.getUpdatedAt().plusMillis(1);

        assertThrows(IllegalStateException.class,
                     () -> deleteJob.deleteIfStale(job.getId(), cutoff));

        assertTrue(exists());
        assertTrue(storage.holds(OBJECT_KEY));
    }

    /**
     * I return the job as the database holds it, so its timestamps have the
     * database's precision.
     */
    private TextractJob stored() {
        em.flush();
        em.clear();
        return repo.findById(job.getId()).orElseThrow();
    }

    private boolean exists() {
        em.flush();
        em.clear();
        return repo.existsById(job.getId());
    }

    private long countLines() {
        return (Long) em.createNativeQuery(COUNT_LINES, Long.class)
                .setParameter(1, job.getId())
                .getSingleResult();
    }

}
