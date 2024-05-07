package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rating implements Identified {
    // Semantics and ids MUST NOT drift apart
    ONE_STAR(1L),
    TWO_STARS(2L),
    THREE_STARS(3L),
    FOUR_STARS(4L),
    FIVE_STARS(5L);

    private final Long id;
}
