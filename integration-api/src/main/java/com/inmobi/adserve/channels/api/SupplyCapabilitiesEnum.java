package com.inmobi.adserve.channels.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum SupplyCapabilitiesEnum {
    AUDIO(1),
    VIDEO(2),
    ORIENTATION(3),
    HTML5(4),
    JS(5),
    VIDEO_AUTOPLAY(60),
    AUDIO_AUTOPLAY(61),
    PLAYABLE(76),
    RM_CUSTOM_FRAME(77),
    INSTANT_PLAY(85),
    MOAT_CAPABILITY(116),
    INLINE_BANNER_VAST(117);

    @Getter
    private final int id;
}
