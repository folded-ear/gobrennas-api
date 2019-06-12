package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Task;
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

    private User user;

    @Before
    public void setUp() {
        user = userRepository.save(new User("Johann", "johann", "johann@example.com", "<HIDDEN>"));
    }

    @Test
    public void createUpdateTimestamps() {
        assertFalse(repo.findByOwnerAndParentIsNull(user).iterator().hasNext());

        Task groceries = new Task(user, "Groceries");
        assertNull(groceries.getCreatedAt());
        assertNull(groceries.getUpdatedAt());
        groceries = repo.save(groceries);

        assertNotNull(groceries.getCreatedAt());
        assertNotNull(groceries.getUpdatedAt());
        assertTrue(repo.findByOwnerAndParentIsNull(user).iterator().hasNext());

        Instant old = groceries.getUpdatedAt();
        assertEquals(old, groceries.getUpdatedAt());

        groceries.setName("Grocery List");
        repo.flush();

        assertNotEquals(old, groceries.getUpdatedAt());
    }

    @Test
    public void findById() {
        Task groceries = new Task(user, "Groceries");
        assertNull(groceries.getId());
        groceries = repo.saveAndFlush(groceries); // because IDENTITY generation, the flush isn't needed, but it's good style
        Long id = groceries.getId();
        assertNotNull(id);
        entityManager.clear(); // so it has to reload

        //noinspection OptionalGetWithoutIsPresent
        assertEquals(groceries.getName(), repo.findById(id).get().getName());
    }

}