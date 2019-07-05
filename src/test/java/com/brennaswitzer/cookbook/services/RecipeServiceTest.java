package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.brennaswitzer.cookbook.util.RecipeBox.PIZZA_CRUST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RecipeServiceTest {

    private RecipeService service;

    @Before
    public void setUp() {
        service = new RecipeService();
    }

    @Test
    public void addRawIngredientsToList() {
        TaskList list = new TaskList("Groceries");
        assertEquals(0, list.getSubtaskCount());
        Consumer<Iterator<Task>> checkItems = itr -> {
            assertEquals("2 c flour", itr.next().getName());
            assertEquals("1 c water", itr.next().getName());
            assertEquals("1 packet yeast", itr.next().getName());
            assertEquals("1 Tbsp sugar", itr.next().getName());
        };

        service.addRawIngredientsToList(PIZZA_CRUST, list, true);
        assertEquals(5, list.getSubtaskCount());
        Iterator<Task> itr = list.getOrderedSubtasksView().iterator();
        assertEquals("Pizza Crust:", itr.next().getName());
        checkItems.accept(itr);
        assertFalse(itr.hasNext());

        service.addRawIngredientsToList(PIZZA_CRUST, list, false);
        assertEquals(5 + 4, list.getSubtaskCount());
        itr = list.getOrderedSubtasksView().iterator();
        assertEquals("Pizza Crust:", itr.next().getName());
        checkItems.accept(itr);
        checkItems.accept(itr);
        assertFalse(itr.hasNext());
    }

}