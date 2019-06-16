package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
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
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User alice, bob, eve;

    @Before
    public void setUp() {
        this.alice = createUser("Alice");
        this.bob = createUser("Bob");
        this.eve = createUser("Eve");
    }

    private User createUser(String name) {
        return userRepository.save(new User(name, name.toLowerCase(), name.toLowerCase() + "@example.com", "<HIDDEN>"));
    }

    @Test
    public void createUpdateTimestamps() {
        assertFalse(repo.findByOwnerAndParentIsNull(alice).iterator().hasNext());

        Task groceries = new TaskList(alice, "Groceries");
        assertNull(groceries.getCreatedAt());
        assertNull(groceries.getUpdatedAt());
        groceries = repo.save(groceries);

        assertNotNull(groceries.getCreatedAt());
        assertNotNull(groceries.getUpdatedAt());
        assertTrue(repo.findByOwnerAndParentIsNull(alice).iterator().hasNext());

        Instant old = groceries.getUpdatedAt();
        assertEquals(old, groceries.getUpdatedAt());

        groceries.setName("Grocery List");
        repo.flush();

        assertNotEquals(old, groceries.getUpdatedAt());
    }

    @Test
    public void findById() {
        Task groceries = new Task("Groceries");
        assertNull(groceries.getId());
        groceries = repo.saveAndFlush(groceries); // because IDENTITY generation, the flush isn't needed, but it's good style
        Long id = groceries.getId();
        assertNotNull(id);
        entityManager.clear(); // so it has to reload

        //noinspection OptionalGetWithoutIsPresent
        assertEquals(groceries.getName(), repo.findById(id).get().getName());
    }

}