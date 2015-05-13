package com.inmobi.adserve.contracts.misc.contentjson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by ishanbhatnagar on 8/5/15.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum NativeAdContentAsset {
    ICON,
    TITLE,
    DESCRIPTION,
    CTA,
    STAR_RATING,
    SCREENSHOT,
    LANDING_URL;

    public boolean isImageAsset() {
        return this == ICON || this == SCREENSHOT;
    }

    public boolean isTextAsset() {
        return this == TITLE || this == DESCRIPTION;
    }

    public boolean isOtherAsset() {
        return !this.isImageAsset() && !this.isTextAsset();
    }
}

