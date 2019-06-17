package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository repo;

    @Autowired
    private EntityManager entityManager;

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