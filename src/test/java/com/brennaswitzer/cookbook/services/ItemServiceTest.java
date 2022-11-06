package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class ItemServiceTest {

    @Autowired
    private ItemService service;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    UserPrincipalAccess principalAccess;

    @Test
    public void recognizeItem() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

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
    public void recognizeItemMultipleNoCase() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "1 cup Italian seasoning";
        RecognizedItem el = service.recognizeItem(RAW);

        Stream<RecognizedItem.Range> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedItem.Range ing = ri.filter(it -> it.getType() == RecognizedItem.Type.ITEM).findFirst().get();
        assertEquals(new RecognizedItem.Range(6, 23, RecognizedItem.Type.ITEM), ing);
    }

    @Test
    public void recognizeItemPunctuation() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "1 cup flour,";
        RecognizedItem el = service.recognizeItem(RAW);

        Stream<RecognizedItem.Range> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedItem.Range ing = ri.filter(it -> it.getType() == RecognizedItem.Type.ITEM).findFirst().get();
        assertEquals(new RecognizedItem.Range(6, 11, RecognizedItem.Type.ITEM), ing);
    }

    @Test
    public void recognizeItemMultipleWords() {
        recognizeChickenThighs(raw ->
                service.recognizeItem(raw));
    }

    private RecognizedItem recognizeChickenThighs(Function<String, RecognizedItem> doRecognition) {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "2 cup chicken thighs";
        RecognizedItem el = doRecognition.apply(RAW);
        assertEquals(RAW, el.getRaw());

        System.out.println(el);

        Iterator<RecognizedItem.Range> ri = el.getRanges().iterator();
        assertEquals(new RecognizedItem.Range(0, 1, RecognizedItem.Type.AMOUNT), ri.next());
        assertEquals(new RecognizedItem.Range(2, 5, RecognizedItem.Type.UNIT), ri.next());
        assertEquals(new RecognizedItem.Range(6, 20, RecognizedItem.Type.ITEM), ri.next());
        assertFalse(ri.hasNext());
        return el;
    }

    @Test
    public void recognizeItemMultipleWordsWithCursorBeforeSpace() {
        recognizeChickenThighs(raw ->
                service.recognizeItem(raw, 13));
    }

    @Test
    public void recognizeItemMultipleWordsWithCursorAfterSpace() {
        recognizeChickenThighs(raw ->
                service.recognizeItem(raw, 14));
    }

    @Test
    public void recognizeAndSuggestSimple() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

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
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'cru'
        RecognizedItem el = service.recognizeItem("1 gram \"cru, dehydrated", 11);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 11, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMidWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'cru'
        RecognizedItem el = service.recognizeItem("1 gram \"crumbs", 11);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 11, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'pizza cru'
        RecognizedItem el = service.recognizeItem("1 gram \"pizza cru, dehydrated", 17);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 17, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWordUnquoted() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'pizza cru'
        RecognizedItem el = service.recognizeItem("1 gram pizza cru, dehydrated", 16);
        Iterator<RecognizedItem.Suggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognizedItem.Suggestion("Pizza Crust",
                new RecognizedItem.Range(7, 16, RecognizedItem.Type.ITEM)), itr.next());
    }

    @Test
    public void buildMultiwordPhrases() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "spanish apple cake";
        RecognizedItem el = service.recognizeItem(RAW);
        Stream<RecognizedItem.Range> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedItem.Range ing = ri.filter(it -> it.getType() == RecognizedItem.Type.ITEM).findFirst().get();
        assertEquals(new RecognizedItem.Range(0, 18, RecognizedItem.Type.ITEM), ing);

    }

}
