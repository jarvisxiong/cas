package com.inmobi.adserve.channels.util.demand.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by ishan.bhatnagar on 8/31/15.
 */

// This should ideally be defined in the API module.
// TODO: Remove dependency of API module on repository module
@RequiredArgsConstructor
public enum DemandAdFormatConstraints {
    STATIC(0),     // Banner, Interstitial and Native(non-video)
    VAST_VIDEO(1),
    UNKNOWN(1000);

    @Getter
    private final int value;

    public static DemandAdFormatConstraints getDemandAdFormatConstraintsByValue(final Integer adTypeTargeted) {
        DemandAdFormatConstraints returnValue = UNKNOWN;

        if (null == adTypeTargeted) {
            returnValue = STATIC;
        } else {
            switch (adTypeTargeted) {
                case 0:
                    returnValue = STATIC;
                    break;
                case 1:
                    returnValue = VAST_VIDEO;
                    break;
            }
        }

        return returnValue;
    }
}
