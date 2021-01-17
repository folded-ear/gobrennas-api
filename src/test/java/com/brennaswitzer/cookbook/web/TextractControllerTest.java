package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.TextractJobInfo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class TextractControllerTest {

    @Autowired
    private TextractController controller;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private User alice;

    @Before
    public void setUp() {
        alice = userRepo.getByName("Alice");
    }

    private Long createJob() {
        TextractJob job = new TextractJob();
        job.setOwner(alice);
        job.setCreatedAt(Instant.now().minusSeconds(43200));
        S3File p = new S3File("yesterday", "blerg", 7L);
        job.setPhoto(p);
        job.setReady(true);
        Set<TextractJob.Line> lines = new HashSet<>();
        lines.add(new TextractJob.Line(
                "fred",
                new TextractJob.Box(0.1, 0.2, 0.3, 0.4)
        ));
        job.setLines(lines);
        entityManager.persist(job);
        entityManager.flush();
        entityManager.clear();
        return job.getId();
    }

    @Test
    public void testSubscribe() {
        assertEquals(0, controller.subscribeToQueue().size());

        createJob();

        List<TextractJobInfo> infos = controller.subscribeToQueue();
        assertEquals(1, infos.size());
        TextractJobInfo info = infos.get(0);
        // this is sorta silly, but LocalStorageService is so coded.
        assertEquals("images/pork_chops.jpg", info.getPhoto().getUrl());
        assertTrue(info.isReady());
        assertNull(info.getLines());
    }

    @Test
    public void testGetJob() {
        Long id = createJob();
        TextractJobInfo info = controller.getJob(id);
        // this is sorta silly, but LocalStorageService is so coded.
        assertEquals("images/pork_chops.jpg", info.getPhoto().getUrl());
        assertTrue(info.isReady());
        assertEquals(1, info.getLines().size());
        TextractJob.Line line = info.getLines().iterator().next();
        assertEquals("fred", line.getText());
        assertEquals(0.1, line.getBox().getLeft(), 0.0001);
        assertEquals(0.4, line.getBox().getHeight(), 0.0001);
    }

}
