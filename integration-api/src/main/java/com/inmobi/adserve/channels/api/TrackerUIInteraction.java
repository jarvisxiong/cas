package com.inmobi.adserve.channels.api;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum TrackerUIInteraction {
    RENDER(18),
    CLICK(8);

    @Getter
    private final int value;
}
