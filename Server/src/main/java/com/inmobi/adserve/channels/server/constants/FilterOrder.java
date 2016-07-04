package com.inmobi.adserve.channels.server.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum FilterOrder {
    FIRST(0),
    SECOND(1),
    THIRD(2),
    FOURTH(3),
    FIFTH(4),
    SIXTH(5),
    SEVENTH(6),

    DEFAULT(100),

    THIRD_LAST(998),
    SECOND_LAST(999),
    LAST(1000);

    @Getter
    private final int value;
}
