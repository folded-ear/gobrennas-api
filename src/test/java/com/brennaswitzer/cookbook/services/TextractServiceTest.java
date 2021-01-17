package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class TextractServiceTest {

    @Autowired
    private TextractService service;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private User alice;

    @Before
    public void setUp() {
        alice = userRepo.getByName("Alice");
    }

    @Test
    public void findByOwner() {
        assertEquals(0, service.getQueue().size());

        TextractJob job = new TextractJob();
        job.setOwner(alice);
        job.setCreatedAt(Instant.now().minusSeconds(43200));
        S3File p = new S3File("yesterday", "blerg", 7L);
        job.setPhoto(p);
        entityManager.persist(job);
        entityManager.flush();
        entityManager.clear();

        List<TextractJob> jobs = service.getQueue();
        assertEquals(1, jobs.size());
        job = jobs.get(0);
        assertEquals("yesterday", job.getPhoto().getObjectKey());
        assertFalse(job.isReady());
    }

}
