package com.inmobi.adserve.channels.api;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum TrackerUIInteraction {
    UNKNOWN(-1),
    CLIENT_FILL(120),
    RENDER(18),
    VIEW(1),
    CLICK(8);

    @Getter
    private final int value;
}