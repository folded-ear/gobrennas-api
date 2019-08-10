package com.brennaswitzer.cookbook.payload;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class RecognizedElement {

    private String raw;
    private Set<Range> ranges;
    private Set<Completion> completions;

    public RecognizedElement() {
    }

    public RecognizedElement(String raw) {
        this.raw = raw;
    }

    public enum Type {
        AMOUNT,
        UNIT,
        NEW_UNIT,
        ITEM,
        NEW_ITEM,
    }

    public static class Range {

        public static Comparator<Range> BY_POSITION = Comparator.comparingInt(a -> a.start);

        private int start;
        private int end;
        private Type type;

        public Range() {
        }

        public Range(int start, int end, Type type) {
            this.start = start;
            this.end = end;
            this.type = type;
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

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Range)) return false;
            Range range = (Range) o;
            return start == range.start &&
                    end == range.end &&
                    type == range.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end, type);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Range{");
            sb.append("start=").append(start);
            sb.append(", end=").append(end);
            sb.append(", type=").append(type);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class Completion {

        public static Comparator<Completion> BY_POSITION = Comparator.comparingInt(a -> a.target.start);

        private String name;
        private Range target;

        public Completion() {
        }

        public Completion(String name, Range target) {
            this.name = name;
            this.target = target;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Range getTarget() {
            return target;
        }

        public void setTarget(Range target) {
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Completion)) return false;
            Completion that = (Completion) o;
            return name.equals(that.name) &&
                    target.equals(that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, target);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Completion{");
            sb.append("name='").append(name).append('\'');
            sb.append(", target=").append(target);
            sb.append('}');
            return sb.toString();
        }
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public Set<Range> getRanges() {
        if (ranges == null) {
            ranges = new TreeSet<>(Range.BY_POSITION);
        }
        return ranges;
    }

    public void setRanges(Set<Range> ranges) {
        this.ranges = ranges;
    }

    public Set<Completion> getCompletions() {
        if (completions == null) {
            completions = new TreeSet<>(Completion.BY_POSITION);
        }
        return completions;
    }

    public void setCompletions(Set<Completion> completions) {
        this.completions = completions;
    }

    public RecognizedElement withRange(Range r) {
        getRanges().add(r);
        return this;
    }

    public RecognizedElement withCompletion(Completion c) {
        getCompletions().add(c);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecognizedElement)) return false;
        RecognizedElement that = (RecognizedElement) o;
        return raw.equals(that.raw) &&
                Objects.equals(ranges, that.ranges) &&
                Objects.equals(completions, that.completions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, ranges, completions);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecognizedElement{");
        sb.append("raw='").append(raw).append('\'');
        sb.append(", ranges=").append(ranges);
        sb.append(", completions=").append(completions);
        sb.append('}');
        return sb.toString();
    }

}
