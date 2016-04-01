package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.server.requesthandler.RequestFilters.CHINA;
import static com.inmobi.adserve.channels.server.requesthandler.RequestFilters.CHINA_MOBILE;
import static com.inmobi.adserve.channels.server.requesthandler.RequestFilters.INMOBI_SLOT_FOR_300x250;
import static com.inmobi.adserve.channels.server.requesthandler.RequestFilters.TEST_CHINA_CARRIER_ID;
import static com.inmobi.adserve.channels.server.requesthandler.RequestFilters.dropInChinaMobileTargetingFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by ishan.bhatnagar on 01/04/16.
 */
public class RequestFiltersTest {
    @DataProvider(name = "Drop In China Mobile Targeting")
    public Object[][] paramDataProviderForChinaMobileTargetingFilter() {
        return new Object[][] {
                {"testBothCountryIdAndProcessedMkSlotsNull", null, null, null, false, null},
                {"testNullCountryId", new ArrayList<Short>(), null, null, false, new ArrayList<Short>()},
                {"testNullProcessedMkSlots", null, CHINA, null, false, null},
                {"testEmptyProcessedMkSlots", new ArrayList<Short>(), CHINA, null, false, new ArrayList<Short>()},
                {"testNullCarrierId", Arrays.asList(INMOBI_SLOT_FOR_300x250), CHINA, null, true, Arrays.asList(INMOBI_SLOT_FOR_300x250)},
                {"testChinaMobileEmptyProcessedSlots", new ArrayList<Short>(), CHINA, CHINA_MOBILE, false, new ArrayList<Short>()},
                {"testChinaMobileProcessedSlotsWithTestSlot", new ArrayList<>(Arrays.asList(INMOBI_SLOT_FOR_300x250)), CHINA, CHINA_MOBILE, false, new ArrayList<Short>()},
                {"testChinaMobileProcessedSlotsWithNonTestSlot", Arrays.asList(INMOBI_SLOT_FOR_300x250 + 1), CHINA, CHINA_MOBILE, false, Arrays.asList(INMOBI_SLOT_FOR_300x250 + 1)},
                {"testTestCarrierProcessedSlotsWithOnlyTestSlot", new ArrayList<>(Arrays.asList(INMOBI_SLOT_FOR_300x250)), CHINA, TEST_CHINA_CARRIER_ID, false, Arrays.asList(INMOBI_SLOT_FOR_300x250)},
                {"testTestCarrierProcessedSlotsWithAtleastOneTestSlot", new ArrayList<>(Arrays.asList(INMOBI_SLOT_FOR_300x250, INMOBI_SLOT_FOR_300x250 + 1)), CHINA, TEST_CHINA_CARRIER_ID, false, Arrays.asList(INMOBI_SLOT_FOR_300x250)},
                {"testTestCarrierProcessedSlotsWithNonTestSlot", new ArrayList<>(Arrays.asList(INMOBI_SLOT_FOR_300x250 + 1)), CHINA, TEST_CHINA_CARRIER_ID, false, new ArrayList<Short>()},
        };
    }

    @Test(dataProvider = "Drop In China Mobile Targeting")
    public void testChinaMobileTargetingFilter(final String testCaseName, final List<Short> processedMkSlot, final Long countryId,
            final Integer carrierId, final boolean expectedOutcome, final List<Short> expectedProcessedMkSlot) throws Exception {
        Assert.assertEquals(expectedOutcome, dropInChinaMobileTargetingFilter(processedMkSlot, countryId, carrierId));
        if (null != processedMkSlot) {
            Assert.assertTrue(processedMkSlot.equals(expectedProcessedMkSlot));
        }
    }
}