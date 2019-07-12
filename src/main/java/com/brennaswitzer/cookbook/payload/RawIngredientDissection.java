package com.brennaswitzer.cookbook.payload;

import javax.validation.constraints.NotNull;

public class RawIngredientDissection {

    @NotNull
    private String raw;
    private Section quantity;
    private Section units;
    @NotNull
    private Section name;
    private String prep;

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
        // `name` is non-null
        return this.name.text;
    }

    public String getPrep() {
        return prep;
    }

    public void setPrep(String prep) {
        this.prep = prep;
    }

    public static class Section {
        private int start;
        private int end;
        private String text;

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
    }

}
