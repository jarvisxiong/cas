package com.inmobi.adserve.channels.api;

import lombok.RequiredArgsConstructor;

/**
 * Created by avinash.kumar on 4/18/16.
 */
@RequiredArgsConstructor
enum TrackerUIInteraction {
    UNKNOWN(-1),
    CLIENT_FILL(120),
    RENDER(1),
    VIEW(18),
    CLICK(8);

    private final int value;

    public int getValue() {
        return this.value;
    }
}
