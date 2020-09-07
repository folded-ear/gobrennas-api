package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
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
    public void recognizeItem() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);

        final String RAW = "3 & 1/2 cup whole wheat flour";
        RecognizedItem el = service.recognizeItem(RAW);
        System.out.println(el);

        assertEquals(RAW, el.getRaw());
        Iterator<RecognizedItem.Range> ri = el.getRanges().iterator();
        assertEquals(new RecognizedItem.Range(0, 7, RecognizedItem.Type.AMOUNT), ri.next());
        assertEquals(new RecognizedItem.Range(8, 11, RecognizedItem.Type.UNIT), ri.next());
        assertEquals(new RecognizedItem.Range(24, 29, RecognizedItem.Type.ITEM), ri.next());
        assertFalse(ri.hasNext());
    }

    @Test
    public void recognizeAndSuggestSimple() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);

        // with no cursor; we're at the end
        RecognizedItem el = service.recognizeItem("1 gram f");
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("flour",
                new RecognizedItem.Range(7, 8, RecognizedItem.Type.ITEM)), itr.next());
        assertEquals(  new RecognizedItem.Suggestion("fresh tomatoes",
                new RecognizedItem.Range(7, 8, RecognizedItem.Type.ITEM)), itr.next());
        assertEquals(new RecognizedItem.Suggestion("Fried Chicken",
                new RecognizedItem.Range(7, 8, RecognizedItem.Type.ITEM)), itr.next());
        assertFalse(itr.hasNext());

        // cursor after the 'fr'
        el = service.recognizeItem("1 gram fr, dehydrated", 9);
        itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("fresh tomatoes",
                new RecognizedItem.Range(7, 9, RecognizedItem.Type.ITEM)), itr.next());
        assertEquals(new RecognizedItem.Suggestion("Fried Chicken",
                new RecognizedItem.Range(7, 9, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestQuoted() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);
        // cursor after the 'cru'
        RecognizedItem el = service.recognizeItem("1 gram \"cru, dehydrated", 11);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 11, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMidWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);
        // cursor after the 'cru'
        RecognizedItem el = service.recognizeItem("1 gram \"crumbs", 11);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 11, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager);
        // cursor after the 'pizza cru'
        RecognizedItem el = service.recognizeItem("1 gram \"pizza cru, dehydrated", 17);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 17, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void findRecipeByNameTest() {
        Recipe recipe = new Recipe("chicken things");
        service.createNewRecipe(recipe);
        Iterable<Recipe> recipes = service.findRecipeByName("chicken");
        System.out.println(recipes);
    }
}
