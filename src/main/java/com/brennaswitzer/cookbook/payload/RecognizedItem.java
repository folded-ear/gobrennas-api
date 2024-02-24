package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.util.EnglishUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RecognizedItem {

    @Getter
    private String raw;

    @Getter
    private int cursor;

    private Set<RecognizedRange> ranges;

    private Set<RecognitionSuggestion> suggestions;

    public RecognizedItem(String raw) {
        this(raw, raw.length());
    }

    public RecognizedItem(String raw, int cursor) {
        this.raw = raw;
        this.cursor = Math.min(Math.max(cursor, 0), raw.length());
    }

    public Set<RecognizedRange> getRanges() {
        if (ranges == null) {
            ranges = new TreeSet<>(RecognizedRange.BY_POSITION);
        }
        return ranges;
    }

    public Set<RecognitionSuggestion> getSuggestions() {
        if (suggestions == null) {
            suggestions = new TreeSet<>(RecognitionSuggestion.BY_POSITION_AND_NAME);
        }
        return suggestions;
    }

    public RecognizedItem withRange(RecognizedRange r) {
        getRanges().add(r);
        return this;
    }

    public RecognizedItem withSuggestion(RecognitionSuggestion c) {
        getSuggestions().add(c);
        return this;
    }

    /**
     * I return an Iterable over all the words in the raw string which have not
     * been recognized yet.
     */
    public Iterable<RecognizedRange> unrecognizedWords() {
        return unrecognizedWords(raw);
    }

    public Iterable<RecognizedRange> unrecognizedWordsThrough(int endIndex) {
        return unrecognizedWords(raw.substring(0, endIndex));
    }

    private Iterable<RecognizedRange> unrecognizedWords(String raw) {
        List<RecognizedRange> result = new LinkedList<>();
        String[] words = raw.split(" ");
        int pos = 0;
        for (String w : words) {
            String c = EnglishUtils.canonicalize(w);
            RecognizedRange r;
            if (w.equals(c)) {
                r = new RecognizedRange(pos, pos + w.length());
            } else {
                int start = w.indexOf(c);
                r = new RecognizedRange(
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
