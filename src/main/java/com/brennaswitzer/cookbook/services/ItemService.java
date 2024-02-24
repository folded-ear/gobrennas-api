package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.MutableItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognitionSuggestion;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.payload.RecognizedRange;
import com.brennaswitzer.cookbook.payload.RecognizedRangeType;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NumberUtils;
import com.brennaswitzer.cookbook.util.RawUtils;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class ItemService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private IngredientService ingredientService;

    public RecognizedItem recognizeItem(String raw) {
        if (raw == null) return null;
        // if no cursor location is specified, assume it's at the end
        return recognizeItem(raw, raw.length());
    }

    public RecognizedItem recognizeItem(String raw, int cursor) {
        return recognizeItem(raw, cursor, true);
    }

    public RecognizedItem recognizeItem(String raw, int cursor, boolean withSuggestions) {
        if (raw == null) return null;
        if (raw.trim().isEmpty()) return null;
        RecognizedItem item = new RecognizedItem(raw, cursor);
        RawIngredientDissection d = RawUtils.dissect(raw);
        RawIngredientDissection.Section secQuantity = d.getQuantity();
        if (secQuantity != null) {
            // there's a quantity
            item.withRange(new RecognizedRange(
                    secQuantity.getStart(),
                    secQuantity.getEnd(),
                    RecognizedRangeType.QUANTITY
            ).withQuantity(NumberUtils.parseNumber(secQuantity.getText())));
        }
        RawIngredientDissection.Section secUnit = d.getUnits();
        if (secUnit != null) {
            // there's an explicit unit
            Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                    entityManager,
                    secUnit.getText());
            item.withRange(new RecognizedRange(
                    secUnit.getStart(),
                    secUnit.getEnd(),
                    ouom.isPresent()
                            ? RecognizedRangeType.UNIT
                            : RecognizedRangeType.NEW_UNIT
            ).withId(ouom.map(UnitOfMeasure::getId).orElse(null)));
        }
        RawIngredientDissection.Section secName = d.getName();
        int idxExplicitItemStart = -1;
        int idxImplicitItemStart = -1;
        if (secName != null) {
            // there's an explicit name
            Optional<? extends Ingredient> oing = ingredientService.findIngredientByName(
                    secName.getText());
            idxExplicitItemStart = secName.getStart();
            item.withRange(new RecognizedRange(
                    secName.getStart(),
                    secName.getEnd(),
                    oing.isPresent()
                            ? RecognizedRangeType.ITEM
                            : RecognizedRangeType.NEW_ITEM
            ).withId(oing.map(Ingredient::getId).orElse(null)));
        } else if (!raw.contains("\"")) {
            // no explicit name, so see if there's an implicit one
            Optional<RecognizedRange> matched = multiPass(item.unrecognizedWords(), raw);
            // TODO: Break out pieces and test for item service
            // This line means that when we have a match, we get no more suggestions, which is not the behavior we want
            // idxNameStart = matched.get().getStart();
            if (matched.isPresent()) {
                RecognizedRange r = matched.get();
                item.withRange(r);
                idxImplicitItemStart = r.getStart();
            }
        }
        if (secQuantity != null && secUnit == null && !raw.contains("_")) {
            // There's a quantity, but no explicit unit, so see if there's an
            // implicit one. But only before the item, if one exists.
            int idxItemStart = Math.max(
                    idxExplicitItemStart,
                    idxImplicitItemStart);
            Iterable<RecognizedRange> wordRanges = idxItemStart < 0
                    ? item.unrecognizedWords()
                    : item.unrecognizedWordsThrough(idxItemStart);
            for (RecognizedRange r : wordRanges) {
                Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                        entityManager,
                        raw.substring(r.getStart(), r.getEnd()));
                if (ouom.isEmpty()) continue;
                item.withRange(r.of(RecognizedRangeType.UNIT).withId(ouom.get().getId()));
                break;
            }
        }
        if (withSuggestions && idxExplicitItemStart < 0) { // there's no name, explicit or implicit
            // based on cursor position, see if we can suggest any names
            // start with looking backwards for a quote
            int start = raw.lastIndexOf('"', item.getCursor());
            val hasQuote = start >= 0;
            boolean hasSpace = false;
            if (start < 0) { // look backwards for a non-trailing space
                int end = item.getCursor() - 1;
                while (end > 0 && Character.isWhitespace(raw.charAt(end)))
                    end--;
                start = raw.lastIndexOf(' ', end);
                hasSpace = true;
            }
            if (start < 0) { // whole prefix, i guess
                start = 0;
                hasSpace = false;
            }
            int replaceStart = hasSpace ? start + 1 : start;
            String search = raw.substring(hasQuote ? replaceStart + 1 : replaceStart, item.getCursor())
                    .trim()
                    .toLowerCase();
            if (!search.isEmpty()) {
                String singularSearch = EnglishUtils.unpluralize(search);
                Iterable<Ingredient> matches = ingredientService.findAllIngredientsByNameContaining(search);
                String lcRawPrefix = raw.toLowerCase()
                        .substring(0, item.getCursor() - search.length());
                StreamSupport.stream(matches.spliterator(), false)
                        .limit(10)
                        .forEach(i -> {
                            // this should probably check all locations the
                            // search matches, not just the first...
                            String lcName = i.getName().toLowerCase();
                            int idx = lcName.indexOf(singularSearch);
                            int len = RawUtils.lengthOfLongestSharedSuffix(
                                    lcName.subSequence(0, idx),
                                    lcRawPrefix
                            );
                            // no leading spaces in the replaced range
                            while (len > 0 && raw.charAt(replaceStart - len) == ' ') {
                                len--;
                            }
                            item.withSuggestion(new RecognitionSuggestion(
                                    i.getName(),
                                    new RecognizedRange(
                                            replaceStart - len,
                                            item.getCursor(),
                                            RecognizedRangeType.ITEM
                                    ).withId(i.getId())
                            ));
                        });
            }
        }
        return item;
    }

    public void updateAutoRecognition(MutableItem it) {
        if (it == null) return;
        it.setIngredient(null);
        it.setQuantity(null);
        it.setPreparation(null);
        autoRecognize(it);
    }

    public void autoRecognize(MutableItem it) {
        if (it == null) return;
        String raw = it.getRaw();
        if (raw == null || raw.trim().isEmpty()) return;
        RecognizedItem recog = recognizeItem(raw);
        if (recog == null) return;
        RawIngredientDissection dissection = RawIngredientDissection
                .fromRecognizedItem(recog);
        if (!dissection.hasName()) return;
        it.setIngredient(ingredientService.ensureIngredientByName(dissection.getNameText()));
        it.setPreparation(dissection.getPrep());
        if (!dissection.hasQuantity()) return;
        Quantity q = new Quantity();
        Double quantity = NumberUtils.parseNumber(dissection.getQuantityText());
        if (quantity == null) return; // couldn't parse?
        q.setQuantity(quantity);
        if (dissection.hasUnits()) {
            q.setUnits(UnitOfMeasure.ensure(entityManager,
                                            EnglishUtils.canonicalize(dissection.getUnitsText())));
        }
        it.setQuantity(q);
    }

    static class Phrase implements Comparable<Phrase> {

        @Getter
        String original;
        @Getter
        String canonical;
        RecognizedRange range;

        public static Comparator<Phrase> BY_POSITION = Comparator.comparingInt(a -> a.range.getStart());

        Phrase(RecognizedRange range, String original) {
            this.range = range;
            setOriginal(original);
        }

        public void setOriginal(String raw) {
            this.original = raw;
            String sanitized = original
                    .trim()
                    .toLowerCase();
            sanitized = EnglishUtils.canonicalize(sanitized);
            sanitized = EnglishUtils.unpluralize(sanitized);
            this.canonical = sanitized;
        }

        public Phrase of(RecognizedRangeType type) {
            return new Phrase(range.of(type), original);
        }

        public Phrase withId(Long id) {
            range.withId(id);
            return this;
        }

        public Phrase merge(Phrase other) {
            return new Phrase(
                    range.merge(other.range),
                    original + " " + other.original
            );
        }

        public int compareTo(Phrase o) {
            assert o != null;
            return Integer.compare(range.getEnd() - range.getStart(), o.range.getEnd() - o.range.getStart());
        }

    }

    public Optional<RecognizedRange> multiPass(Iterable<RecognizedRange> ranges, String raw) {

        List<Phrase> rs = StreamSupport
                .stream(ranges.spliterator(), false)
                .map(it -> new Phrase(it, raw.substring(it.getStart(), it.getEnd())))
                .collect(Collectors.toList());

        List<String> words = rs
                .stream()
                .map(Phrase::getCanonical)
                .collect(Collectors.toList());

        Iterable<Ingredient> options = ingredientService.findAllIngredientsByNamesContaining(words);

        List<Phrase> phrases = buildPhrases(rs);
        Phrase best = null;

        for (Phrase phrase : phrases) {
            for (Ingredient opt : options) {
                Phrase match = phrase.of(RecognizedRangeType.ITEM).withId(opt.getId());
                if (phrase.getCanonical().equals(opt.getName()) || phrase.getOriginal()
                        .equalsIgnoreCase(opt.getName())) {
                    if (best == null) {
                        best = match;
                    } else {
                        if (phrase.compareTo(best) > 0) {
                            best = match;
                        } else if (phrase.compareTo(best) == 0) {
                            // if the same number of words, which one happens first?
                            if (phrases.indexOf(phrase) > phrases.indexOf(best)) {
                                best = match;
                            }
                        }
                    }
                }
            }
        }

        return best == null ? Optional.empty() : Optional.of(best.range);
    }

    private List<Phrase> buildPhrases(List<Phrase> phrases) {
        List<Phrase> options = new ArrayList<>(phrases);

        for (int i = 0; i < phrases.size(); i++) {
            for (int j = i + 1; j < phrases.size(); j++) {
                Optional<Phrase> phrase = phrases
                        .subList(i, j + 1)
                        .stream()
                        .reduce(Phrase::merge);
                phrase.ifPresent(options::add);
            }
        }
        options.sort(Phrase.BY_POSITION);
        return options;
    }

}
