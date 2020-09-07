package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.IngredientRepository;
import com.brennaswitzer.cookbook.util.NumberUtils;
import com.brennaswitzer.cookbook.util.RawUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class ItemService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private IngredientRepository ingredientRepository;

    public RecognizedItem recognizeItem(String raw) {
        if (raw == null) return null;
        // if no cursor location is specified, assume it's at the end
        return recognizeItem(raw, raw.length());
    }

    public RecognizedItem recognizeItem(String raw, int cursor) {
        if (raw == null) return null;
        if (raw.trim().isEmpty()) return null;
        RecognizedItem el = new RecognizedItem(raw, cursor);
        RawIngredientDissection d = RawUtils.dissect(raw);
        RawIngredientDissection.Section secAmount = d.getQuantity();
        if (secAmount != null) {
            // there's an amount
            el.withRange(new RecognizedItem.Range(
                    secAmount.getStart(),
                    secAmount.getEnd(),
                    RecognizedItem.Type.AMOUNT
            ).withValue(NumberUtils.parseNumber(secAmount.getText())));
        }
        RawIngredientDissection.Section secUnit = d.getUnits();
        if (secUnit != null) {
            // there's an explicit unit
            Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                    entityManager,
                    secUnit.getText());
            el.withRange(new RecognizedItem.Range(
                    secUnit.getStart(),
                    secUnit.getEnd(),
                    ouom.isPresent()
                            ? RecognizedItem.Type.UNIT
                            : RecognizedItem.Type.NEW_UNIT,
                    ouom.map(UnitOfMeasure::getId).orElse(null)
            ));
        }
        RawIngredientDissection.Section secName = d.getName();
        int idxNameStart = -1;
        if (secName != null) {
            // there's an explicit name
            Optional<? extends Ingredient> oing = ingredientService.findIngredientByName(
                    secName.getText());
            idxNameStart = secName.getStart();
            el.withRange(new RecognizedItem.Range(
                    secName.getStart(),
                    secName.getEnd(),
                    oing.isPresent()
                            ? RecognizedItem.Type.ITEM
                            : RecognizedItem.Type.NEW_ITEM,
                    oing.map(Ingredient::getId).orElse(null)
            ));
        } else if (!raw.contains("\"")) {
            // no name, so see if there's an implicit one
            for (RecognizedItem.Range r : el.unrecognizedWords()) {
                Optional<? extends Ingredient> oing = ingredientService.findIngredientByName(
                        raw.substring(r.getStart(), r.getEnd()));
                if (!oing.isPresent()) continue;
                idxNameStart = r.getStart();
                el.withRange(r.of(RecognizedItem.Type.ITEM).withValue(oing.get().getId()));
                break;
            }
        }
        if (secAmount != null && secUnit == null && !raw.contains("_")) {
            // there's an amount, but no explicit unit, so see if there's an implicit one
            for (RecognizedItem.Range r : el.unrecognizedWords()) {
                // unit must precede name, so abort if we get there
                if (idxNameStart >= 0 && idxNameStart < r.getStart()) break;
                Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                        entityManager,
                        raw.substring(r.getStart(), r.getEnd()));
                if (!ouom.isPresent()) continue;
                el.withRange(r.of(RecognizedItem.Type.UNIT).withValue(ouom.get().getId()));
                break;
            }
        }
        if (idxNameStart < 0) { // there's no name, explicit or implicit
            // based on cursor position, see if we can suggest any names
            // start with looking backwards for a quote
            int start = raw.lastIndexOf('"', el.getCursor());
            boolean hasQuote = true;
            boolean hasSpace = false;
            if (start < 0) { // look backwards for a space
                start = raw.lastIndexOf(' ', el.getCursor());
                hasQuote = false;
                hasSpace = true;
            }
            if (start < 0) { // whole prefix, i guess
                start = 0;
                hasQuote = false;
                hasSpace = false;
            }
            int replaceStart = hasSpace ? start + 1 : start;
            String search = raw.substring(hasQuote ? replaceStart + 1 : replaceStart, el.getCursor())
                    .trim()
                    .toLowerCase();
            if (!search.isEmpty()) {
                Iterable<Ingredient> matches = ingredientRepository.findByNameContains(search);
                StreamSupport.stream(matches.spliterator(), false)
                        .limit(10)
                        .forEach(i -> el.withSuggestion(new RecognizedItem.Suggestion(
                                i.getName(),
                                new RecognizedItem.Range(
                                        replaceStart,
                                        el.getCursor(),
                                        RecognizedItem.Type.ITEM,
                                        i.getId()
                                )
                        )));
            }
        }
        return el;
    }

}
