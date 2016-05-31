package com.inmobi.adserve.channels.server.auction;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;


public final class AuctionEngineHelperTest {

    /**
     * Assumptions: demandDensity <= longTermRevenue demandDensity <= highestBid publisherYield >= 1
     */
    @DataProvider(name = "DataProviderForClearingPrice")
    public Object[][] paramDataProviderForClearingPrice() {
        return new Object[][] {
                {"testClearingPriceUpperBoundLowerThanHighestBid", 1.5, 0.0, 1.0, 10, 1.0},
                {"testPublisherYieldIsLargeAndUpperBoundIsLowerThanHighestBid", 1.5, 0.0, 1.0, 1000000, 1.0},
                {"testPublisherYieldIsLarge", 1.5, 0.0, 2.0, 1000000, 1.5},
                {"testClearingPriceLowerBoundIsEqualToHighestBid", 1.5, 1.5, 2.0, 10, 1.5},
                {"testClearingPriceUpperBoundIsEqualToHighestBid", 1.5, 0.0, 1.5, 10, 1.5},
                {"testClearingPriceLowerBoundIsEqualToUpperBound", 1.5, 1.0, 1.0, 100, 1.0},
                {"testClearingPriceStandardCase", 1.5, 0.0, 2.0, 101, 1.4851485148514851}
        };
    }


    @Test(dataProvider = "DataProviderForClearingPrice")
    public void testGetClearingPrice(final String useCaseName, final double highestBid, final double demandDensity,
            final double longTermRevenue, final int publisherYield, final double expectedClearingPrice)
            throws Exception {
        final CasInternalRequestParameters casParams = new CasInternalRequestParameters();
        casParams.setDemandDensity(demandDensity);
        casParams.setLongTermRevenue(longTermRevenue);
        casParams.setPublisherYield(publisherYield);

        Assert.assertEquals(AuctionEngineHelper.getClearingPrice(highestBid, casParams), expectedClearingPrice);
    }

}