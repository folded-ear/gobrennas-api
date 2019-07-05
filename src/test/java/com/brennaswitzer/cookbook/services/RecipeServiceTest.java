package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.brennaswitzer.cookbook.util.RecipeBox.PIZZA;
import static com.brennaswitzer.cookbook.util.RecipeBox.PIZZA_CRUST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class RecipeServiceTest {

    @Autowired
    private RecipeService service;

    @Autowired
    private TaskListRepository listRepo;

    @Autowired
    private UserRepository userRepository;

    private User alice;

    @Before
    public void setUp() {
        alice = userRepository.getByName("Alice");
    }

    @Test
    public void addRawIngredientsToList() {
        TaskList list = listRepo.save(new TaskList(alice, "Groceries"));
        assertEquals(0, list.getSubtaskCount());
        Consumer<Iterator<Task>> checkItems = itr -> {
            assertEquals("2 c flour", itr.next().getName());
            assertEquals("1 c water", itr.next().getName());
            assertEquals("1 packet yeast", itr.next().getName());
            assertEquals("1 Tbsp sugar", itr.next().getName());
        };

        service.addRawIngredientsToList(PIZZA_CRUST, list, true);
        assertEquals(1 + 4, list.getSubtaskCount());
        Iterator<Task> itr = list.getOrderedSubtasksView().iterator();
        assertEquals("Pizza Crust:", itr.next().getName());
        checkItems.accept(itr);
        assertFalse(itr.hasNext());

        service.addRawIngredientsToList(PIZZA_CRUST, list, false);
        assertEquals(1 + 4 + 4, list.getSubtaskCount());
        itr = list.getOrderedSubtasksView().iterator();
        assertEquals("Pizza Crust:", itr.next().getName());
        checkItems.accept(itr);
        checkItems.accept(itr);
        assertFalse(itr.hasNext());
    }

    @Test
    public void addPurchaseableSchmankiesToList() {
        TaskList list = listRepo.save(new TaskList(alice, "Groceries"));
        assertEquals(0, list.getSubtaskCount());
        Consumer<Iterator<Task>> checkItems = itr -> {
            // pizza
            assertEquals("4 oz pepperoni", itr.next().getName());
            // sauce
            assertEquals("1 lbs fresh tomatoes", itr.next().getName());
            assertEquals("1 (6 oz) can tomato paste", itr.next().getName());
            assertEquals("italian seasoning", itr.next().getName());
            // crust
            assertEquals("2 c flour", itr.next().getName());
            assertEquals("1 c water", itr.next().getName());
            assertEquals("1 packet yeast", itr.next().getName());
            assertEquals("1 Tbsp sugar", itr.next().getName());
        };

        service.addPurchaseableSchmankiesToList(PIZZA, list, false);
        assertEquals(8, list.getSubtaskCount());
        Iterator<Task> itr = list.getOrderedSubtasksView().iterator();
        checkItems.accept(itr);
        assertFalse(itr.hasNext());

        service.addPurchaseableSchmankiesToList(PIZZA, list, true);
        assertEquals(8 + 1 + 8, list.getSubtaskCount());
        itr = list.getOrderedSubtasksView().iterator();
        checkItems.accept(itr);
        assertEquals("Pizza:", itr.next().getName());
        checkItems.accept(itr);
        assertFalse(itr.hasNext());
    }

}