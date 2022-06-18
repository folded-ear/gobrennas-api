package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
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

    private User alice, bob, eve;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
        bob = userRepo.getByName("Bob");
        eve = userRepo.getByName("Eve");
    }

    @Test
    public void createUpdateTimestamps() throws InterruptedException {
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
        Thread.sleep(123);
        repo.flush();

        assertNotEquals(old, groceries.getUpdatedAt());
    }

    @Test
    public void findAccessibleLists() {
        Iterable<TaskList> lists = repo.findAccessibleLists(alice.getId());
        Iterator<TaskList> itr = lists.iterator();
        assertFalse(itr.hasNext());

        TaskList alicesList = repo.save(new TaskList(alice, "Alice's List"));
        TaskList bobsList = repo.save(new TaskList(bob, "Bob's List"));

        lists = repo.findAccessibleLists(alice.getId());
        itr = lists.iterator();
        assertEquals("Alice's List", itr.next().getName());
        assertFalse(itr.hasNext());

        alicesList.getAcl().setGrant(bob, AccessLevel.CHANGE);
        alicesList.getAcl().setGrant(eve, AccessLevel.CHANGE);
        bobsList.getAcl().setGrant(alice, AccessLevel.CHANGE);

        lists = repo.findAccessibleLists(alice.getId());
        itr = lists.iterator();
        assertEquals("Alice's List", itr.next().getName());
        assertEquals("Bob's List", itr.next().getName());
        assertFalse(itr.hasNext());
    }

}
