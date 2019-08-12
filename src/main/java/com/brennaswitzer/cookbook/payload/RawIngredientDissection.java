package com.brennaswitzer.cookbook.payload;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class RawIngredientDissection {

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

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public Section getQuantity() {
        return quantity;
    }

    public void setQuantity(Section quantity) {
        this.quantity = quantity;
    }

    public String getQuantityText() {
        if (this.quantity == null) return null;
        return this.quantity.text;
    }

    public Section getUnits() {
        return units;
    }

    public void setUnits(Section units) {
        this.units = units;
    }

    public String getUnitsText() {
        if (this.units == null) return null;
        return this.units.text;
    }

    public Section getName() {
        return name;
    }

    public void setName(Section name) {
        this.name = name;
    }

    public String getNameText() {
        if (this.name == null) return null;
        return this.name.text;
    }

    public String getPrep() {
        return prep;
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
            final StringBuilder sb = new StringBuilder("Section{");
            sb.append("start=").append(start);
            sb.append(", end=").append(end);
            sb.append(", text='").append(text).append('\'');
            sb.append('}');
            return sb.toString();
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
        final StringBuilder sb = new StringBuilder("RawIngredientDissection{");
        sb.append("raw='").append(raw).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append(", units=").append(units);
        sb.append(", name=").append(name);
        sb.append(", prep='").append(prep).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
