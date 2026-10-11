package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TextractJobStatus implements Identified {

    PENDING(0L),
    READY(1L),
    ERROR(2L);

    private final Long id;

}
