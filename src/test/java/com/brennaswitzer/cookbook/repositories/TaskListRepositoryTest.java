package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class TaskListRepositoryTest {

    @Autowired
    private TaskListRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepo;

    private User alice;

    @Before
    public void setUp() {
        alice = userRepo.getByName("Alice");
    }

    @Test
    public void createUpdateTimestamps() {
        assertFalse(repo.findByOwner(alice).iterator().hasNext());

        TaskList groceries = new TaskList(alice, "Groceries");
        assertNull(groceries.getCreatedAt());
        assertNull(groceries.getUpdatedAt());
        groceries = repo.save(groceries);

        assertNotNull(groceries.getCreatedAt());
        assertNotNull(groceries.getUpdatedAt());
        assertTrue(repo.findByOwner(alice).iterator().hasNext());

        Instant old = groceries.getUpdatedAt();
        assertEquals(old, groceries.getUpdatedAt());

        groceries.setName("Grocery List");
        repo.flush();

        assertNotEquals(old, groceries.getUpdatedAt());
    }

}