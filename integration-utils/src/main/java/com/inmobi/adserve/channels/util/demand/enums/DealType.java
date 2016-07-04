package com.inmobi.adserve.channels.util.demand.enums;

import org.apache.commons.lang.StringUtils;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum DealType {
    RIGHT_TO_FIRST_REFUSAL,
    PRIVATE_AUCTION,
    PREFERRED;

    public static DealType getDealTypeByName(final String dealTypeStr) {
        DealType returnValue = PREFERRED;

        if (StringUtils.isNotBlank(dealTypeStr)) {
            switch (dealTypeStr) {
                case "RIGHT_TO_FIRST_REFUSAL":
                    returnValue = RIGHT_TO_FIRST_REFUSAL;
                    break;
                case "RIGHT_TO_FIRST_REFUSAL_DEAL":
                    returnValue = RIGHT_TO_FIRST_REFUSAL;
                    break;
                case "PRIVATE_AUCTION":
                    returnValue = PRIVATE_AUCTION;
                    break;
                case "PRIVATE_AUCTION_DEAL":
                    returnValue = PRIVATE_AUCTION;
                    break;
                case "PREFERRED":
                    returnValue = PREFERRED;
                    break;
                case "PREFERRED_DEAL":
                    returnValue = PREFERRED;
                    break;
                default:
                    // Default is PREFERRED
                    break;
            }
        }
        return returnValue;
    }
}
