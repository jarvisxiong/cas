package com.inmobi.adserve.channels.adnetworks.ix;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.entity.IXVideoTrafficEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

public class IXAdNetworkHelperTest {
    private static RepositoryHelper mockRepositoryHelper;
    private static IXVideoTrafficEntity mockIxVideoTrafficEntity;
    private static final String siteIdWithCountryEntry = "siteId1";
    private static final String siteIdWithoutCountryEntry = "siteId2";
    private static final String siteIdWithoutSiteEntry = "siteId3";
    private static final String siteIdWithoutSiteAndCountryEntry = "siteId4";
    private static final Integer countryWithEntry = 94;
    private static final Integer countryWithoutEntry = 95;
    private static final int defaultTrafficPercentage = 50;

    @BeforeClass
    public static void setUp() {
        mockRepositoryHelper = createMock(RepositoryHelper.class);
        mockIxVideoTrafficEntity = createMock(IXVideoTrafficEntity.class);

        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithCountryEntry, countryWithEntry))
                .andReturn(mockIxVideoTrafficEntity).anyTimes();

        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithoutCountryEntry, countryWithEntry))
                .andReturn(null).anyTimes();
        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithoutCountryEntry, -1))
                .andReturn(mockIxVideoTrafficEntity).anyTimes();

        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithoutSiteEntry, countryWithEntry))
                .andReturn(null).anyTimes();
        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithoutSiteEntry, -1))
                .andReturn(null).anyTimes();
        expect(mockRepositoryHelper.queryIXVideoTrafficRepository("", countryWithEntry))
                .andReturn(mockIxVideoTrafficEntity).anyTimes();

        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithoutSiteAndCountryEntry,
                countryWithoutEntry)).andReturn(null).anyTimes();
        expect(mockRepositoryHelper.queryIXVideoTrafficRepository(siteIdWithoutSiteAndCountryEntry, -1))
                .andReturn(null).anyTimes();
        expect(mockRepositoryHelper.queryIXVideoTrafficRepository("", countryWithoutEntry))
                .andReturn(null).anyTimes();

        expect(mockIxVideoTrafficEntity.getTrafficPercentage()).andReturn((short)60).anyTimes();

        replay(mockRepositoryHelper, mockIxVideoTrafficEntity);
    }

    @DataProvider(name = "VideoTrafficPercentageDataProvider")
    public Object[][] videoTrafficPercentDataProvider() {
        return new Object[][] {
                {"testBothSiteAndCountryMatch", siteIdWithCountryEntry, countryWithEntry, 60},
                {"testOnlySiteMatches", siteIdWithoutCountryEntry, countryWithEntry, 60},
                {"testOnlyCountryMatches", siteIdWithoutSiteEntry, countryWithEntry, 60},
                {"testNoMatch", siteIdWithoutSiteAndCountryEntry, countryWithoutEntry, defaultTrafficPercentage}
        };
    }

    @Test(dataProvider = "VideoTrafficPercentageDataProvider")
    public void testGetIXVideoTrafficPercentage(final String testCaseName, final String siteId, final Integer countryId,
            final int expectedTrafficPercentage) throws Exception {
        assertThat(IXAdNetworkHelper.getIXVideoTrafficPercentage(siteId, countryId, mockRepositoryHelper,
                        defaultTrafficPercentage), is(expectedTrafficPercentage));
    }
}