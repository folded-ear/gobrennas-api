package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.RecognizedElement;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.function.Consumer;

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

    @Autowired
    private EntityManager entityManager;

    private User alice;

    @Before
    public void setUp() {
        alice = userRepository.getByName("Alice");
    }

    @Test
    public void assembleShoppingList() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);

        TaskList list = listRepo.save(new TaskList(alice, "Groceries"));
        assertEquals(0, list.getSubtaskCount());
        Consumer<Iterator<Task>> checkItems = itr -> {
            // pizza
            // pepperoni is a raw ingredient
            // sauce
            assertEquals("fresh tomatoes (1 lbs)", itr.next().getName());
            assertEquals("tomato paste (1 (6 oz) can)", itr.next().getName());
            assertEquals("italian seasoning", itr.next().getName());
            assertEquals("salt (1.5 tsp)", itr.next().getName());
            // crust
            assertEquals("flour (2 c)", itr.next().getName());
            assertEquals("water (1 c)", itr.next().getName());
            assertEquals("yeast (1 packet)", itr.next().getName());
            assertEquals("sugar (1 Tbsp)", itr.next().getName());
            // raw
            assertEquals("pepperoni", itr.next().getName());
        };

        service.assembleShoppingList(box.pizza, list, false);
        assertEquals(9, list.getSubtaskCount());
        Iterator<Task> itr = list.getOrderedSubtasksView().iterator();
        checkItems.accept(itr);
        assertFalse(itr.hasNext());

        service.assembleShoppingList(box.pizza, list, true);
        assertEquals(9 + 1 + 9, list.getSubtaskCount());
        itr = list.getOrderedSubtasksView().iterator();
        checkItems.accept(itr);
        assertEquals("Pizza:", itr.next().getName());
        checkItems.accept(itr);
        assertFalse(itr.hasNext());
    }

    @Test
    public void recognizeElement() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);

        final String RAW = "3 & 1/2 cup whole wheat flour";
        RecognizedElement el = service.recognizeElement(RAW);
        System.out.println(el);

        assertEquals(RAW, el.getRaw());
        Iterator<RecognizedElement.Range> ri = el.getRanges().iterator();
        assertEquals(new RecognizedElement.Range(0, 7, RecognizedElement.Type.AMOUNT), ri.next());
        assertEquals(new RecognizedElement.Range(8, 11, RecognizedElement.Type.UNIT), ri.next());
        assertEquals(new RecognizedElement.Range(24, 29, RecognizedElement.Type.ITEM), ri.next());
        assertFalse(ri.hasNext());
        Iterator<RecognizedElement.Suggestion> si = el.getSuggestions().iterator();
        // currently we just randomly suggest something for every unknown word...
        assertEquals(new RecognizedElement.Range(12, 17, RecognizedElement.Type.ITEM), si.next().getTarget());
        assertEquals(new RecognizedElement.Range(18, 23, RecognizedElement.Type.ITEM), si.next().getTarget());
        assertFalse(si.hasNext());
    }

}