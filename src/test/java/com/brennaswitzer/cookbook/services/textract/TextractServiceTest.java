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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class TextractServiceTest {

    @Autowired TextractService service;
    @Autowired TextractJobRepository repo;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private TextractJob job;

    @BeforeEach
    void setUp() {
        job = new TextractJob();
        job.setOwner(principal.getUser());
        job.setPhoto(new S3File("photo", "image/jpeg", 1L));
        em.persist(job);
    }

    @Test
    void retryableErrorBecomesPending() {
        job.fail("Rate exceeded", true);
        em.flush();

        TextractJob returned = service.retryJob(job.getId());

        assertEquals(job, returned);
        TextractJob j = reload();
        assertEquals(TextractJobStatus.PENDING, j.getStatus());
        assertNull(j.getErrorMessage());
        assertFalse(j.isRetryable());
    }

    @Test
    void nonRetryableErrorIsRejected() {
        job.fail("Bad document", false);
        em.flush();

        assertThrows(JobNotRetryableException.class,
                     () -> service.retryJob(job.getId()));

        TextractJob j = reload();
        assertEquals(TextractJobStatus.ERROR, j.getStatus());
        assertEquals("Bad document", j.getErrorMessage());
        assertFalse(j.isRetryable());
    }

    @Test
    void pendingJobIsRejected() {
        em.flush();

        assertThrows(JobNotRetryableException.class,
                     () -> service.retryJob(job.getId()));

        assertEquals(TextractJobStatus.PENDING, reload().getStatus());
    }

    @Test
    void readyJobIsRejected() {
        job.succeed(Set.of());
        em.flush();

        assertThrows(JobNotRetryableException.class,
                     () -> service.retryJob(job.getId()));

        assertTrue(reload().isReady());
    }

    private TextractJob reload() {
        em.flush();
        em.clear();
        return repo.findById(job.getId()).orElseThrow();
    }

}
