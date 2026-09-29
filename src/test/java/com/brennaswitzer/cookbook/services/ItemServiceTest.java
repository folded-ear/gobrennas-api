package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecognitionChoice;
import com.brennaswitzer.cookbook.payload.RecognitionKind;
import com.brennaswitzer.cookbook.payload.RecognitionSuggestion;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.payload.RecognizedRange;
import com.brennaswitzer.cookbook.payload.RecognizedRangeType;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        recognizeItem("", 0);
        recognizeItem("cat", 0);
        recognizeItem("cat  ", 3);
        recognizeItem(" cat", 2);
        recognizeItem(" cat", 1);
    }

    @Test
    public void recognizeItem() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "3 & 1/2 cup whole wheat flour";
        RecognizedItem el = recognizeItem(RAW);
        System.out.println(el);

        assertEquals(RAW, el.getRaw());
        Iterator<RecognizedRange> ri = el.getRanges().iterator();
        assertEquals(new RecognizedRange(0, 7, RecognizedRangeType.QUANTITY), ri.next());
        assertEquals(new RecognizedRange(8, 11, RecognizedRangeType.UNIT), ri.next());
        assertEquals(new RecognizedRange(24, 29, RecognizedRangeType.ITEM), ri.next());
        assertFalse(ri.hasNext());
    }

    @Test
    public void recognizeImplicitSynonym() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "sea nacl, ground fine";
        RecognizedItem el = recognizeItem(RAW);
        System.out.println(el);

        assertEquals(RAW, el.getRaw());
        Iterator<RecognizedRange> ri = el.getRanges().iterator();
        RecognizedRange saltRange = ri.next();
        assertEquals(new RecognizedRange(4, 8, RecognizedRangeType.ITEM), saltRange);
        assertEquals(box.salt.getId(), saltRange.getId());
        assertFalse(ri.hasNext());
    }

    @Test
    public void recognizeExplicitSynonym() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "sea \"nacl\", ground fine";
        RecognizedItem el = recognizeItem(RAW);
        System.out.println(el);

        assertEquals(RAW, el.getRaw());
        Iterator<RecognizedRange> ri = el.getRanges().iterator();
        RecognizedRange saltRange = ri.next();
        assertEquals(box.salt.getId(), saltRange.getId());
        assertFalse(ri.hasNext());
    }

    @Test
    public void recognizeItemMultipleNoCase() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "1 cup Italian seasoning";
        RecognizedItem el = recognizeItem(RAW);

        Stream<RecognizedRange> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedRange ing = ri.filter(it -> it.getType() == RecognizedRangeType.ITEM).findFirst().get();
        assertEquals(new RecognizedRange(6, 23, RecognizedRangeType.ITEM), ing);
    }

    @Test
    public void recognizeItemLongestPhraseWins() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "42 cup chicken thighs with italian seasoning";
        RecognizedItem el = recognizeItem(RAW);

        Iterator<RecognizedRange> itr = el.getRanges().iterator();
        assertEquals("42", itr.next().of(RAW));
        assertEquals("cup", itr.next().of(RAW));
        assertEquals("italian seasoning", itr.next().of(RAW));
        assertFalse(itr.hasNext());
    }

    @Test
    public void recognizeItemFirstSameLengthPhraseWins() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "7 cup each pizza crust and pizza sauce, blended";
        //                             |-- 11 ---|     |-- 11 ---|
        RecognizedItem el = recognizeItem(RAW);

        Iterator<RecognizedRange> itr = el.getRanges().iterator();
        assertEquals("7", itr.next().of(RAW));
        assertEquals("cup", itr.next().of(RAW));
        assertEquals("pizza crust", itr.next().of(RAW));
        assertFalse(itr.hasNext());
    }

    @Test
    public void recognizeItemPunctuation() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "1 cup flour,";
        RecognizedItem el = recognizeItem(RAW);

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
        recognizeChickenThighs(this::recognizeItem);
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
                                       recognizeItem(raw, 0));
    }

    @Test
    public void recognizeItemMultipleWordsWithCursorBeforeSpace() {
        recognizeChickenThighs(raw ->
                                       recognizeItem(raw, 13));
    }

    @Test
    public void recognizeItemMultipleWordsWithCursorAfterSpace() {
        recognizeChickenThighs(raw ->
                                       recognizeItem(raw, 14));
    }

    @Test
    public void recognizeAndSuggestSimple() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        RecognizedItem el = recognizeItem("1 gram f");
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("flour",
                                               new RecognizedRange(7, 8, RecognizedRangeType.ITEM), RecognitionKind.PANTRY_ITEM, null), itr.next());
        assertEquals(new RecognitionSuggestion("fresh tomatoes",
                                               new RecognizedRange(7, 8, RecognizedRangeType.ITEM), RecognitionKind.PANTRY_ITEM, null), itr.next());
        assertEquals(new RecognitionSuggestion("Fried Chicken",
                                               new RecognizedRange(7, 8, RecognizedRangeType.ITEM), RecognitionKind.RECIPE, null), itr.next());
        assertFalse(itr.hasNext());

        // cursor after the 'fr'
        el = recognizeItem("1 gram fr, dehydrated", 9);
        itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("fresh tomatoes",
                                               new RecognizedRange(7, 9, RecognizedRangeType.ITEM), RecognitionKind.PANTRY_ITEM, null), itr.next());
        assertEquals(new RecognitionSuggestion("Fried Chicken",
                                               new RecognizedRange(7, 9, RecognizedRangeType.ITEM), RecognitionKind.RECIPE, null), itr.next());
    }

    @Test
    public void recognizeAndSuggestQuoted() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'cru'
        RecognizedItem el = recognizeItem("1 gram \"cru, dehydrated", 11);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 11, RecognizedRangeType.ITEM), RecognitionKind.RECIPE, null), itr.next());
    }

    @Test
    public void recognizeAndSuggestMidWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'cru'
        RecognizedItem el = recognizeItem("1 gram \"crumbs", 11);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 11, RecognizedRangeType.ITEM), RecognitionKind.RECIPE, null), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWord() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'pizza cru'
        RecognizedItem el = recognizeItem("1 gram \"pizza cru, dehydrated", 17);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 17, RecognizedRangeType.ITEM), RecognitionKind.RECIPE, null), itr.next());
    }

    @Test
    public void recognizeAndSuggestMultiWordUnquoted() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        // cursor after the 'pizza cru'
        RecognizedItem el = recognizeItem("1 gram pizza cru, dehydrated", 16);
        Iterator<RecognitionSuggestion> itr = el.getSuggestions().iterator();
        assertEquals(new RecognitionSuggestion("Pizza Crust",
                                               new RecognizedRange(7, 16, RecognizedRangeType.ITEM), RecognitionKind.RECIPE, null), itr.next());
    }

    @Test
    public void buildMultiwordPhrases() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        final String RAW = "spanish apple cake";
        RecognizedItem el = recognizeItem(RAW);
        Stream<RecognizedRange> ri = el.getRanges().stream();
        //noinspection OptionalGetWithoutIsPresent
        RecognizedRange ing = ri.filter(it -> it.getType() == RecognizedRangeType.ITEM).findFirst().get();
        assertEquals(new RecognizedRange(0, 18, RecognizedRangeType.ITEM), ing);
    }

    @Test
    void sameNamedSuggestionsRetainKindsAndIdsIncludingSections() {
        PantryItem pantry = new PantryItem("stock");
        entityManager.persist(pantry);
        Recipe parent = new Recipe();
        parent.setName("Soup");
        parent.setOwner(principalAccess.getUser());
        entityManager.persist(parent);
        Recipe recipe = new Recipe();
        recipe.setName("stock");
        recipe.setOwner(principalAccess.getUser());
        entityManager.persist(recipe);
        Recipe section = new Recipe();
        section.setName("stock");
        section.setOwner(principalAccess.getUser());
        parent.addOwnedSection(section);
        entityManager.persist(section);
        entityManager.flush();

        var options = service.getSuggestions(new RecognizedItem("2 cups sto"), 10, true);
        assertEquals(List.of(RecognitionKind.PANTRY_ITEM, RecognitionKind.RECIPE, RecognitionKind.SECTION),
                     options.stream().map(RecognitionSuggestion::getKind).toList());
        assertEquals(List.of(pantry.getId(), recipe.getId(), section.getId()),
                     options.stream().map(s -> s.getTarget().getId()).toList());
        assertEquals("Soup", options.get(2).getDetail());
        assertTrue(options.stream().allMatch(s -> s.getTarget().of("2 cups sto").equals("sto")));
        assertEquals(1, service.recognizeItem("sto", 3, true).getSuggestions().size());

        String raw = "2 cups stock, reduced";
        var chosen = service.recognizeItem(raw, raw.length(), false,
                                          new RecognitionChoice(section.getId(), 7, 12));
        var name = chosen.getRanges().stream().filter(r -> r.getType() == RecognizedRangeType.ITEM).findFirst().orElseThrow();
        assertEquals(section.getId(), name.getId());
        assertEquals("stock", name.of(raw));
        assertThrows(IllegalArgumentException.class, () -> service.recognizeItem(raw, 0, false,
                new RecognitionChoice(section.getId(), 0, 5)));
        assertThrows(IllegalArgumentException.class, () -> service.recognizeItem(raw, 0, false,
                new RecognitionChoice(section.getId(), 7, 100)));
    }

    private RecognizedItem recognizeItem(String raw) {
        if (raw == null) return null;
        // if no cursor location is specified, assume it's at the end
        return service.recognizeItem(raw, raw.length(), true);
    }

    private RecognizedItem recognizeItem(String raw, int cursor) {
        return service.recognizeItem(raw, cursor, true);
    }


}
