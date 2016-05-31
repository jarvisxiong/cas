package com.inmobi.adserve.channels.util.demand.enums;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum AuctionType {
    FIRST_PRICE(1, com.inmobi.adserve.adpool.AuctionType.FIRST_PRICE),
    SECOND_PRICE(2, com.inmobi.adserve.adpool.AuctionType.SECOND_PRICE);

    @Getter
    private final int value;
    @Getter
    private final com.inmobi.adserve.adpool.AuctionType getUMPAuctionType;

    public static AuctionType getAuctionTypeByName(final String auctionTypeStr) {
        AuctionType returnValue = FIRST_PRICE;

        if (StringUtils.isNotBlank(auctionTypeStr)) {
            switch (auctionTypeStr) {
                case "FIRST_PRICE":
                    break;
                case "SECOND_PRICE":
                    returnValue = SECOND_PRICE;
                    break;
                default:
                    // Default is FIRST_PRICE
                    break;
            }
        }
        return returnValue;
    }
}
