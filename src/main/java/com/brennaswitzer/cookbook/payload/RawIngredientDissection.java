package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;

@Setter
@Getter
public class RawIngredientDissection {

    // this is "duplicated" as processRecognizedItem
    public static RawIngredientDissection fromRecognizedItem(RecognizedItem it) {
        Optional<RecognizedItem.Range> qr = it.getRanges().stream()
                .filter(r -> RecognizedItem.Type.AMOUNT.equals(r.getType()))
                .findFirst();
        Optional<RecognizedItem.Range> ur = it.getRanges().stream()
                .filter(r -> RecognizedItem.Type.UNIT.equals(r.getType()) || RecognizedItem.Type.NEW_UNIT.equals(r.getType()))
                .findFirst();
        Optional<RecognizedItem.Range> nr = it.getRanges().stream()
                .filter(r -> RecognizedItem.Type.ITEM.equals(r.getType()) || RecognizedItem.Type.NEW_ITEM.equals(r.getType()))
                .findFirst();

        Function<String, String> stripMarkers = s -> {
            if (s == null) return s;
            if (s.length() < 3) return s;
            char c = s.charAt(0);
            if (c != s.charAt(s.length() - 1)) return s;
            if (Character.isLetterOrDigit(c)) return s;
            return s.substring(1, s.length() - 1);
        };

        Function<Optional<RecognizedItem.Range>, Section> sectionFromRange = or ->
                or.map(r -> new Section(
                                stripMarkers.apply(
                                        it.getRaw().substring(r.getStart(), r.getEnd())),
                                r.getStart(),
                                r.getEnd())
                        )
                        .orElse(null);

        List<Optional<RecognizedItem.Range>> ranges = new ArrayList<>(3);
        ranges.add(qr);
        ranges.add(ur);
        ranges.add(nr);
        String p = ranges.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt(RecognizedItem.Range::getStart).reversed())
                .sequential()
                .reduce(
                        it.getRaw(),
                        (s, r) -> s.substring(0, r.getStart()) + s.substring(r.getEnd()),
                        (a, b) -> { throw new UnsupportedOperationException(); })
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("^\\s*,", "");

        RawIngredientDissection d = new RawIngredientDissection(it.getRaw());
        d.quantity = sectionFromRange.apply(qr);
        d.units = sectionFromRange.apply(ur);
        d.name = sectionFromRange.apply(nr);
        d.prep = p;
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

    public static class Section {
        private int start;
        private int end;
        private String text;

        public Section() {
        }

        public Section(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Section)) return false;
            Section section = (Section) o;
            return start == section.start &&
                    end == section.end &&
                    text.equals(section.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end, text);
        }

        @Override
        public String toString() {
            return "Section{" + "start=" + start +
                    ", end=" + end +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawIngredientDissection)) return false;
        RawIngredientDissection that = (RawIngredientDissection) o;
        return raw.equals(that.raw) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(units, that.units) &&
                Objects.equals(name, that.name) &&
                Objects.equals(prep, that.prep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, quantity, units, name, prep);
    }

    @Override
    public String toString() {
        return "RawIngredientDissection{" + "raw='" + raw + '\'' +
                ", quantity=" + quantity +
                ", units=" + units +
                ", name=" + name +
                ", prep='" + prep + '\'' +
                '}';
    }
}
