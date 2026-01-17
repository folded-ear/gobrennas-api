package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.util.RawUtils;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class RawIngredientDissection {

    private static final Pattern RE_CANON_SPACES = Pattern.compile("\\s+");
    private static final Pattern RE_CANON_COMMAS = Pattern.compile("\\s*(,\\s*)+");
    private static final Pattern RE_LEADING_COMMAS = Pattern.compile("^,\\s*");

    // this is "duplicated" as processRecognizedItem
    public static RawIngredientDissection fromRecognizedItem(RecognizedItem it) {
        Optional<RecognizedRange> qr = it.getRanges().stream()
                .filter(r -> RecognizedRangeType.QUANTITY == r.getType())
                .findFirst();
        Optional<RecognizedRange> ur = it.getRanges().stream()
                .filter(r -> RecognizedRangeType.UNIT == r.getType()
                             || RecognizedRangeType.NEW_UNIT == r.getType())
                .findFirst();
        Optional<RecognizedRange> nr = it.getRanges().stream()
                .filter(r -> RecognizedRangeType.ITEM == r.getType()
                             || RecognizedRangeType.NEW_ITEM == r.getType())
                .findFirst();

        Function<Optional<RecognizedRange>, Section> sectionFromRange = or ->
                or.map(r -> new Section(
                                RawUtils.stripMarkers(
                                        it.getRaw().substring(r.getStart(), r.getEnd())),
                                r.getStart(),
                                r.getEnd())
                        )
                        .orElse(null);

        List<Optional<RecognizedRange>> ranges = new ArrayList<>(3);
        ranges.add(qr);
        ranges.add(ur);
        ranges.add(nr);
        String prep = ranges.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt(RecognizedRange::getStart).reversed())
                .reduce(
                        it.getRaw(),
                        (s, r) -> s.substring(0, r.getStart()) + s.substring(r.getEnd()),
                        (a, b) -> {throw new UnsupportedOperationException();})
                .trim();
        prep = RE_CANON_SPACES.matcher(prep).replaceAll(" ");
        prep = RE_CANON_COMMAS.matcher(prep).replaceAll(", ");
        prep = RE_LEADING_COMMAS.matcher(prep).replaceFirst("");

        RawIngredientDissection d = new RawIngredientDissection(it.getRaw());
        d.quantity = sectionFromRange.apply(qr);
        d.units = sectionFromRange.apply(ur);
        d.name = sectionFromRange.apply(nr);
        d.prep = prep;
        return d;
    }

    @NotNull
    private String raw;
    private Section quantity;
    private Section units;
    private Section name;
    private String prep;

    public RawIngredientDissection() {
    }

    public RawIngredientDissection(String raw) {
        this.raw = raw;
    }

    public boolean hasQuantity() {
        return quantity != null;
    }

    public String getQuantityText() {
        if (this.quantity == null) return null;
        return this.quantity.text;
    }

    public boolean hasUnits() {
        return units != null;
    }

    public String getUnitsText() {
        if (this.units == null) return null;
        return this.units.text;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getNameText() {
        if (this.name == null) return null;
        return this.name.text;
    }

    public void setPrep(String prep) {
        if (prep != null) prep = prep.trim();
        this.prep = prep == null || prep.isEmpty() ? null : prep;
    }

    @Value
    public static class Section {
        String text;
        int start;
        int end;
    }

}
