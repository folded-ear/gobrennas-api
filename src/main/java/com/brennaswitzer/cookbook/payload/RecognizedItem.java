package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.util.EnglishUtils;
import lombok.*;

import java.util.*;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RecognizedItem {

    @Getter
    @Setter
    private String raw;

    @Getter
    @Setter
    private int cursor;

    private Set<Range> ranges;

    private Set<Suggestion> suggestions;

    public RecognizedItem(String raw) {
        this(raw, raw.length());
    }

    public RecognizedItem(String raw, int cursor) {
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class Range {

        public static Comparator<Range> BY_POSITION = Comparator.comparingInt(a -> a.start);

        private int start;
        private int end;
        private Type type;
        @EqualsAndHashCode.Exclude
        private Object value;

        public Range(int start, int end) {
            this(start, end, Type.UNKNOWN);
        }

        public Range(int start, int end, Type type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        public Range of(Type type) {
            return new Range(
                    getStart(),
                    getEnd(),
                    type
            );
        }

        public Range merge(Range other) {
            return new Range(
                    getStart(),
                    other.getEnd()
            );
        }

        @SuppressWarnings("RedundantIfStatement")
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

        public Range withValue(Object value) {
            setValue(value);
            return this;
        }

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class Suggestion {

        public static Comparator<Suggestion> BY_POSITION = Comparator.comparingInt(a -> a.target.start);
        public static Comparator<Suggestion> BY_POSITION_AND_NAME = BY_POSITION.thenComparing(a -> a.name, String.CASE_INSENSITIVE_ORDER);

        private String name;
        private Range target;

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

    public RecognizedItem withRange(Range r) {
        getRanges().add(r);
        return this;
    }

    public RecognizedItem withSuggestion(Suggestion c) {
        getSuggestions().add(c);
        return this;
    }

    /**
     * I return an Iterable over all the words in the raw string which have not
     * been recognized yet.
     */
    public Iterable<Range> unrecognizedWords() {
        return unrecognizedWords(raw);
    }

    public Iterable<Range> unrecognizedWordsThrough(int endIndex) {
        return unrecognizedWords(raw.substring(0, endIndex));
    }

    private Iterable<Range> unrecognizedWords(String raw) {
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
