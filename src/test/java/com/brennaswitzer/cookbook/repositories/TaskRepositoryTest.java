package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Task;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository repo;

    @Autowired
    private TaskListRepository listRepo;

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
    public void findById() {
        TaskList groceries = new TaskList(alice, "Groceries");
        listRepo.save(groceries);

        Task oj = new Task("OJ").of(groceries);
        assertNull(oj.getId());
        oj = repo.saveAndFlush(oj); // because IDENTITY generation, the flush isn't needed, but it's good style
        Long id = oj.getId();
        assertNotNull(id);
        entityManager.clear(); // so it has to reload

        //noinspection OptionalGetWithoutIsPresent
        assertEquals(oj.getName(), repo.findById(id).get().getName());
    }

}
