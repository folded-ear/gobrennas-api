package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.TextractJobStatus;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class TextractProcessorTest {

    private static final TextractJob.Line LINE = new TextractJob.Line(
            "one cup flour",
            new TextractJob.Box(0.1, 0.2, 0.3, 0.4));

    @Autowired TextractJobRepository repo;
    @Autowired TransactionTemplate txTemplate;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private TextractJob job;

    @BeforeEach
    void setUp() {
        job = new TextractJob();
        job.setOwner(principal.getUser());
        job.setPhoto(new S3File("photo", "image/jpeg", 1L));
        em.persist(job);
        em.flush();
    }

    @Test
    void extractedLinesMakeTheJobReady() {
        process(photo -> Set.of(LINE));

        TextractJob j = reload();
        assertEquals(TextractJobStatus.READY, j.getStatus());
        assertEquals(Set.of(LINE), j.getLines());
        assertNull(j.getErrorMessage());
        assertFalse(j.isRetryable());
    }

    @Test
    void providerReceivesTheJobsPhoto() {
        process(photo -> Set.of(new TextractJob.Line(
                photo.getObjectKey(),
                LINE.getBox())));

        TextractJob.Line line = reload().getLines()
                .iterator()
                .next();
        assertEquals("photo", line.getText());
    }

    @Test
    void retryableFailureMakesARetryableError() {
        process(photo -> {
            throw new TextractFailure("Rate exceeded", true, null);
        });

        TextractJob j = reload();
        assertEquals(TextractJobStatus.ERROR, j.getStatus());
        assertEquals("Rate exceeded", j.getErrorMessage());
        assertTrue(j.isRetryable());
    }

    @Test
    void permanentFailureMakesANonRetryableError() {
        process(photo -> {
            throw new TextractFailure("Bad document", false, null);
        });

        TextractJob j = reload();
        assertEquals(TextractJobStatus.ERROR, j.getStatus());
        assertEquals("Bad document", j.getErrorMessage());
        assertFalse(j.isRetryable());
    }

    @Test
    void unexpectedExceptionMakesAnUnknownError() {
        process(photo -> {
            throw new IllegalStateException("oops");
        });

        TextractJob j = reload();
        assertEquals(TextractJobStatus.ERROR, j.getStatus());
        assertEquals(TextractFailure.UNKNOWN_ERROR, j.getErrorMessage());
        assertFalse(j.isRetryable());
    }

    @Test
    void jobDeletedMidFlightIsLeftAlone() {
        assertDoesNotThrow(() -> process(photo -> {
            repo.delete(job);
            repo.flush();
            return Set.of(LINE);
        }));

        em.clear();
        assertTrue(repo.findById(job.getId()).isEmpty());
    }

    @Test
    void jobNoLongerPendingIsLeftAlone() {
        process(photo -> {
            job.fail("Already failed", true);
            return Set.of(LINE);
        });

        TextractJob j = reload();
        assertEquals(TextractJobStatus.ERROR, j.getStatus());
        assertEquals("Already failed", j.getErrorMessage());
        assertTrue(j.isRetryable());
    }

    @Test
    void unknownJobIsANoOp() {
        var processor = new TextractProcessor(
                photo -> Set.of(LINE),
                repo,
                txTemplate);

        assertDoesNotThrow(
                () -> processor.processJob(new JobRequestedEvent(-1L)));
    }

    private void process(TextractProvider provider) {
        new TextractProcessor(provider, repo, txTemplate)
                .processJob(new JobRequestedEvent(job.getId()));
    }

    private TextractJob reload() {
        em.flush();
        em.clear();
        return repo.findById(job.getId()).orElseThrow();
    }

}
