package com.inmobi.adserve.channels.adnetworks.ix;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.IXBlocklistEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;

public class IXAdNetworkHelperTest {
    private static RepositoryHelper mockRepositoryHelper;

    private static final String siteId = "siteId";
    private static final Long siteIncId = 1L;
    private static final Long countryId = 2L;
    private static String countryIdString;

    private static SASRequestParameters sasParams;
    private static SASRequestParameters sasParamsFS;

    private static RepositoryHelper repositoryHelperWithOnlySiteEntries;
    private static RepositoryHelper repositoryHelperWithOnlyCountryEntries;
    private static RepositoryHelper repositoryHelperWithBothSiteAndCountryEntries;
    private static RepositoryHelper repositoryHelperWithNoEntries;
    private static RepositoryHelper repositoryHelperWithEmptyEntries;

    private static IXBlocklistEntity siteAdvertiserBlocklistEntity;
    private static IXBlocklistEntity siteIndustryBlocklistEntity;
    private static IXBlocklistEntity siteCreativeAttributesBlocklistEntity;
    private static IXBlocklistEntity countryAdvertiserBlocklistEntity;
    private static IXBlocklistEntity countryIndustryBlocklistEntity;
    private static IXBlocklistEntity countryCreativeAttributesBlocklistEntity;

    private static IXBlocklistEntity emptySiteAdvertiserBlocklistEntity;
    private static IXBlocklistEntity emptySiteIndustryBlocklistEntity;
    private static IXBlocklistEntity emptySiteCreativeAttributesBlocklistEntity;

    @BeforeClass
    public static void setUp() {
        countryIdString = String.valueOf(countryId);

        sasParams = new SASRequestParameters();
        sasParams.setSiteId(siteId);
        sasParams.setSiteIncId(siteIncId);
        sasParams.setCountryId(countryId);
        sasParams.setSiteContentType(ContentType.PERFORMANCE);

        sasParamsFS = new SASRequestParameters();
        sasParamsFS.setSiteId(siteId);
        sasParamsFS.setSiteIncId(siteIncId);
        sasParamsFS.setCountryId(countryId);
        sasParamsFS.setSiteContentType(ContentType.FAMILY_SAFE);


        siteAdvertiserBlocklistEntity = new IXBlocklistEntity("s-a", null, null, null, 1, null);
        siteIndustryBlocklistEntity = new IXBlocklistEntity("s-i", null, null, null, 1, null);
        siteCreativeAttributesBlocklistEntity = new IXBlocklistEntity("s-c", null, null, null, 1, null);
        countryAdvertiserBlocklistEntity = new IXBlocklistEntity("c-a", null, null, null, 1, null);
        countryIndustryBlocklistEntity = new IXBlocklistEntity("c-i", null, null, null, 1, null);
        countryCreativeAttributesBlocklistEntity = new IXBlocklistEntity("c-c", null, null, null, 1, null);
        emptySiteAdvertiserBlocklistEntity = new IXBlocklistEntity("should not be present", null, null, null, 0, null);
        emptySiteIndustryBlocklistEntity = new IXBlocklistEntity("should not be present", null, null, null, 0, null);
        emptySiteCreativeAttributesBlocklistEntity = new IXBlocklistEntity("should not be present", null, null, null,
                0, null);

        repositoryHelperWithOnlySiteEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithOnlyCountryEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithBothSiteAndCountryEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithNoEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithEmptyEntries = createNiceMock(RepositoryHelper.class);

        mockRepositoryHelper = createMock(RepositoryHelper.class);

        expect(repositoryHelperWithOnlySiteEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.ADVERTISERS)).andReturn(siteAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithOnlySiteEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(siteIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithOnlySiteEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(siteCreativeAttributesBlocklistEntity).anyTimes();

        expect(repositoryHelperWithOnlyCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY,
                IXBlocklistType.ADVERTISERS)).andReturn(countryAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithOnlyCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(countryIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithOnlyCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(countryCreativeAttributesBlocklistEntity).anyTimes();

        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(siteId,
                IXBlocklistKeyType.SITE,
                IXBlocklistType.ADVERTISERS)).andReturn(siteAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(siteId,
                IXBlocklistKeyType.SITE,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(siteIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(siteId,
                IXBlocklistKeyType.SITE,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(siteCreativeAttributesBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY,
                IXBlocklistType.ADVERTISERS)).andReturn(countryAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(countryIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(countryCreativeAttributesBlocklistEntity).anyTimes();

        expect(repositoryHelperWithEmptyEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.ADVERTISERS)).andReturn(emptySiteAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithEmptyEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(emptySiteIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithEmptyEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(emptySiteCreativeAttributesBlocklistEntity).anyTimes();

        replay(repositoryHelperWithOnlySiteEntries, repositoryHelperWithOnlyCountryEntries,
                repositoryHelperWithBothSiteAndCountryEntries, repositoryHelperWithNoEntries,
                repositoryHelperWithEmptyEntries, mockRepositoryHelper);
    }

    @DataProvider(name = "BlocklistDataProvider")
    public Object[][] blocklistDataProvider() {
        return new Object[][] {
                {"testOnlySiteBlocklists", sasParams, repositoryHelperWithOnlySiteEntries,
                        ImmutableList.of("blk1", "s-a", "s-i", "s-c")},
                {"testOnlyCountryBlocklists", sasParams, repositoryHelperWithOnlyCountryEntries,
                        ImmutableList.of("blk1", "c-a", "c-i", "c-c")},
                {"testBothSiteAndCountryBlocklists", sasParams, repositoryHelperWithBothSiteAndCountryEntries,
                        ImmutableList.of("blk1", "s-a", "s-i", "s-c")},
                {"testOnlyGlobalBlocklistsPERF", sasParams, repositoryHelperWithNoEntries,
                        ImmutableList.of("blk1", "InMobiPERFAdv", "InMobiPERFInd", "InMobiPERFCre")},
                {"testOnlyGlobalBlocklistsFS", sasParamsFS, repositoryHelperWithNoEntries,
                        ImmutableList.of("blk1", "InMobiFSAdv", "InMobiFSInd", "InMobiFSCre")},
                {"testEmptySiteBlocklists", sasParams, repositoryHelperWithEmptyEntries,
                        ImmutableList.of("blk1")},
        };
    }

    @Test(dataProvider = "BlocklistDataProvider")
    public void testGetBlocklists(final String testCaseName, final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final List<String> expectedBlocklists) throws Exception {
        assertEquals(IXAdNetworkHelper.getBlocklists(sasParams, repositoryHelper, null), expectedBlocklists);
    }

}
