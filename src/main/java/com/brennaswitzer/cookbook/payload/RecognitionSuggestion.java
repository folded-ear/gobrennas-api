package com.brennaswitzer.cookbook.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Comparator;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RecognitionSuggestion {

    public static Comparator<RecognitionSuggestion> BY_POSITION = Comparator.comparing(a -> a.target,
                                                                                       RecognizedRange.BY_POSITION);
    public static Comparator<RecognitionSuggestion> BY_POSITION_AND_NAME = BY_POSITION.thenComparing(a -> a.name,
                                                                                                     String.CASE_INSENSITIVE_ORDER);

    private String name;
    private RecognizedRange target;

}
