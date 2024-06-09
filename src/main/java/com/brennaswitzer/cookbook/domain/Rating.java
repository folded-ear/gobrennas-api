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

    public static Rating fromId(Long id) {
        return switch (id == null ? 0 : id.intValue()) {
            case 1 -> ONE_STAR;
            case 2 -> TWO_STARS;
            case 3 -> THREE_STARS;
            case 4 -> FOUR_STARS;
            case 5 -> FIVE_STARS;
            default -> throw new IllegalArgumentException("Invalid numeric rating: " + id);
        };
    }
}
