package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.payload.RecognitionSuggestion;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.payload.RecognizedRange;
import com.brennaswitzer.cookbook.payload.RecognizedRangeType;
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
    public void whitespaces() {
        service.recognizeItem("", 0);
        service.recognizeItem("cat", 0);
        service.recognizeItem("cat  ", 3);
        service.recognizeItem(" cat", 2);
        service.recognizeItem(" cat", 1);
    }

    @Test
    public void recognizeItem() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "3 & 1/2 cup whole wheat flour";
        RecognizedItem el = service.recognizeItem(RAW);
        System.out.println(el);

        assertEquals(RAW, el.getRaw());
        Iterator<RecognizedRange> ri = el.getRanges().iterator();
        assertEquals(new RecognizedRange(0, 7, RecognizedRangeType.QUANTITY), ri.next());
        assertEquals(new RecognizedRange(8, 11, RecognizedRangeType.UNIT), ri.next());
        assertEquals(new RecognizedRange(24, 29, RecognizedRangeType.ITEM), ri.next());
        assertFalse(ri.hasNext());
    }

    @Test
    public void recognizeItemMultipleNoCase() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "1 cup Italian seasoning";
        RecognizedItem el = service.recognizeItem(RAW);

        Stream<RecognizedRange> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedRange ing = ri.filter(it -> it.getType() == RecognizedRangeType.ITEM).findFirst().get();
        assertEquals(new RecognizedRange(6, 23, RecognizedRangeType.ITEM), ing);
    }

    @Test
    public void recognizeItemPunctuation() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "1 cup flour,";
        RecognizedItem el = service.recognizeItem(RAW);

        Stream<RecognizedRange> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedRange ing = ri.filter(it -> it.getType() == RecognizedRangeType.ITEM).findFirst().get();
        assertEquals(new RecognizedRange(6, 11, RecognizedRangeType.ITEM), ing);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void multiNumberRecog() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        String raw = "12 flour, grated (~3 cups or 19 Tbsp)";

        var recog = service.recognizeItem(raw, raw.length(), false);

        var q = recog.getRanges()
                .stream()
                .filter(r -> r.getType() == RecognizedRangeType.QUANTITY)
                .findFirst()
                .get();
        assertEquals("12", raw.substring(q.getStart(), q.getEnd()));
        var item = recog.getRanges()
                .stream()
                .filter(r -> r.getType() == RecognizedRangeType.ITEM)
                .findFirst()
                .get();
        assertEquals("flour", raw.substring(item.getStart(), item.getEnd()));
        var optUnit = recog.getRanges()
                .stream()
                .filter(r -> r.getType() == RecognizedRangeType.UNIT)
                .findFirst();
        assertFalse(optUnit.isPresent());
    }

    @Test
    public void recognizeItemMultipleWords() {
        recognizeChickenThighs(raw ->
                service.recognizeItem(raw));
    }

    private void recognizeChickenThighs(Function<String, RecognizedItem> doRecognition) {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "2 cup chicken thighs";
        RecognizedItem el = doRecognition.apply(RAW);
        assertEquals(RAW, el.getRaw());

        System.out.println(el);

        Iterator<RecognizedRange> ri = el.getRanges().iterator();
        assertEquals(new RecognizedRange(0, 1, RecognizedRangeType.QUANTITY), ri.next());
        assertEquals(new RecognizedRange(2, 5, RecognizedRangeType.UNIT), ri.next());
        assertEquals(new RecognizedRange(6, 20, RecognizedRangeType.ITEM), ri.next());
        assertFalse(ri.hasNext());
    }

    @Test
    public void recognizeItemMultipleWordsWithCursorAtStart() {
        recognizeChickenThighs(raw ->
                service.recognizeItem(raw, 0));
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
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("flour",
                                               new RecognizedRange(7, 8, RecognizedRangeType.ITEM)), itr.next());
        assertEquals(new RecognitionSuggestion("fresh tomatoes",
                                               new RecognizedRange(7, 8, RecognizedRangeType.ITEM)), itr.next());
        assertEquals(new RecognitionSuggestion("Fried Chicken",
                                               new RecognizedRange(7, 8, RecognizedRangeType.ITEM)), itr.next());
        assertFalse(itr.hasNext());

        // cursor after the 'fr'
        el = service.recognizeItem("1 gram fr, dehydrated", 9);
        itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("fresh tomatoes",
                                               new RecognizedRange(7, 9, RecognizedRangeType.ITEM)), itr.next());
        assertEquals(new RecognitionSuggestion("Fried Chicken",
                                               new RecognizedRange(7, 9, RecognizedRangeType.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestQuoted() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'cru'
        RecognizedItem el = service.recognizeItem("1 gram \"cru, dehydrated", 11);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 11, RecognizedRangeType.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMidWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'cru'
        RecognizedItem el = service.recognizeItem("1 gram \"crumbs", 11);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 11, RecognizedRangeType.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'pizza cru'
        RecognizedItem el = service.recognizeItem("1 gram \"pizza cru, dehydrated", 17);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 17, RecognizedRangeType.ITEM)), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWordUnquoted() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'pizza cru'
        RecognizedItem el = service.recognizeItem("1 gram pizza cru, dehydrated", 16);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 16, RecognizedRangeType.ITEM)), itr.next());
    }

    @Test
    public void buildMultiwordPhrases() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "spanish apple cake";
        RecognizedItem el = service.recognizeItem(RAW);
        Stream<RecognizedRange> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedRange ing = ri.filter(it -> it.getType() == RecognizedRangeType.ITEM).findFirst().get();
        assertEquals(new RecognizedRange(0, 18, RecognizedRangeType.ITEM), ing);

    }

}
