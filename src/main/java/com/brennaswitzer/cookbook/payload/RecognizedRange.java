package com.brennaswitzer.cookbook.payload;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Comparator;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RecognizedRange {

    public static Comparator<RecognizedRange> BY_POSITION = Comparator.comparingInt(a -> a.start);

    private int start;
    private int end;
    private RecognizedRangeType type;
    @EqualsAndHashCode.Exclude
    private Double quantity;
    @EqualsAndHashCode.Exclude
    private Long id;

    public RecognizedRange(int start, int end) {
        this(start, end, RecognizedRangeType.UNKNOWN);
    }

    public RecognizedRange(int start, int end, RecognizedRangeType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public RecognizedRange of(RecognizedRangeType type) {
        return new RecognizedRange(start, end, type);
    }

    public String of(String raw) {
        return raw.substring(start, end);
    }

    public int length() {
        return end - start;
    }

    public RecognizedRange merge(RecognizedRange other) {
        return new RecognizedRange(
                getStart(),
                other.getEnd()
        );
    }

    public boolean overlaps(RecognizedRange r) {
        boolean startsBeforeEnd = r.start < end;
        boolean endsBeforeStart = r.end <= start;
        return startsBeforeEnd ^ endsBeforeStart;
    }

    public RecognizedRange withQuantity(Double quantity) {
        setQuantity(quantity);
        return this;
    }

    public RecognizedRange withId(Long id) {
        setId(id);
        return this;
    }

}
