package com.inmobi.adserve.channels.util.demand.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


// This should ideally be defined in the API module or in cas-thrift
// TODO: Remove dependency of API module on repository module
@RequiredArgsConstructor
public enum SecondaryAdFormatConstraints {
    ALL(-1),
    STATIC(0),     // Banner, Interstitial and Native(non-video)
    VAST_VIDEO(1),
    REWARDED_VAST_VIDEO(2),
    PURE_VAST(3),
    UNKNOWN(1000);

    @Getter
    private final int value;

    public static SecondaryAdFormatConstraints getDemandAdFormatConstraintsByValue(final Integer adTypeTargeted) {
        SecondaryAdFormatConstraints returnValue = UNKNOWN;

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
                case 2:
                    returnValue = REWARDED_VAST_VIDEO;
                    break;
                case 3:
                    returnValue = PURE_VAST;
                    break;
                case -1:
                    returnValue = ALL;
                    break;
            }
        }

        return returnValue;
    }
}
