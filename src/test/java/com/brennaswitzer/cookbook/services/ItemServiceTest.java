package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class ItemServiceTest {

    @Autowired
    private ItemService service;

    @Autowired
    private EntityManager entityManager;

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

}
