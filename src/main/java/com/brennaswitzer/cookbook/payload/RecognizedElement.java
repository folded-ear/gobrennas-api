package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.util.EnglishUtils;

import java.util.*;

public class RecognizedElement {

    private String raw;
    private int cursor;
    private Set<Range> ranges;
    private Set<Suggestion> suggestions;

    public RecognizedElement() {
    }

    public RecognizedElement(String raw) {
        this(raw, raw.length());
    }

    public RecognizedElement(String raw, int cursor) {
        this.raw = raw;
        this.cursor = Math.min(Math.max(cursor, 0), raw.length());
    }

    public enum Type {
        UNKNOWN,
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
        private Object value;

        public Range() {
        }

        public Range(int start, int end) {
            this(start, end, Type.UNKNOWN);
        }

        public Range(int start, int end, Type type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        public Range(int start, int end, Type type, Object value) {
            this(start, end, type);
            this.value = value;
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

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Range of(Type type) {
            return new Range(
                    getStart(),
                    getEnd(),
                    type
            );
        }

        public boolean overlaps(Range r) {
            // this wraps r
            if (this.start <= r.start && this.end >= r.end) return true;
            // this is inside r
            if (this.start >= r.start && this.end <= r.end) return true;
            // this spans r's start
            if (this.start <= r.start && this.end >= r.start) return true;
            // this spans r's end
            if (this.start <= r.end && this.end >= r.end) return true;
            // no overlap
            return false;
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

        public Range withValue(Object value) {
            setValue(value);
            return this;
        }
    }

    public static class Suggestion {

        public static Comparator<Suggestion> BY_POSITION = Comparator.comparingInt(a -> a.target.start);
        public static Comparator<Suggestion> BY_POSITION_AND_NAME = BY_POSITION.thenComparing(a -> a.name, String.CASE_INSENSITIVE_ORDER);

        private String name;
        private Range target;

        public Suggestion() {
        }

        public Suggestion(String name, Range target) {
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
            if (!(o instanceof Suggestion)) return false;
            Suggestion that = (Suggestion) o;
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

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
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

    public Set<Suggestion> getSuggestions() {
        if (suggestions == null) {
            suggestions = new TreeSet<>(Suggestion.BY_POSITION_AND_NAME);
        }
        return suggestions;
    }

    public void setSuggestions(Set<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public RecognizedElement withRange(Range r) {
        getRanges().add(r);
        return this;
    }

    public RecognizedElement withSuggestion(Suggestion c) {
        getSuggestions().add(c);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecognizedElement)) return false;
        RecognizedElement that = (RecognizedElement) o;
        return raw.equals(that.raw) &&
                cursor == that.cursor &&
                Objects.equals(ranges, that.ranges) &&
                Objects.equals(suggestions, that.suggestions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, cursor, ranges, suggestions);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecognizedElement{");
        sb.append("raw='").append(raw).append('\'');
        sb.append(", cursor=").append(cursor);
        sb.append(", ranges=").append(ranges);
        sb.append(", suggestions=").append(suggestions);
        sb.append('}');
        return sb.toString();
    }

    /**
     * I return an Iterable over all the words in the raw element which have not
     * been recognized yet.
     */
    public Iterable<Range> unrecognizedWords() {
        List<Range> result = new LinkedList<>();
        String[] words = raw.split(" ");
        int pos = 0;
        for (String w : words) {
            String c = EnglishUtils.canonicalize(w);
            Range r;
            if (w.equals(c)) {
                r = new Range(pos, pos + w.length());
            } else {
                int start = w.indexOf(c);
                r = new Range(
                        pos + start,
                        pos + start + c.length()
                );
            }
            if (ranges == null || ranges.stream().noneMatch(r::overlaps)) {
                result.add(r);
            }
            pos += w.length() + 1; // for the split space
        }
        return result;
    }

}
