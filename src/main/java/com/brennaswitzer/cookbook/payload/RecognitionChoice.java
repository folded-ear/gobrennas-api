package com.brennaswitzer.cookbook.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** An explicitly selected identity and its UTF-16 range in the current raw text. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionChoice {
    private Long id;
    private int start;
    private int end;
}
