package com.inmobi.adserve.channels.server.requesthandler.filters;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupDailyImpressionCountFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupMaxSegmentPerRequestFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupPropertyViolationFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupSiteExclusionFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupSupplyDemandClassificationFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserExcludedFilter;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.ConfigurationLoader;

import junit.framework.TestCase;

// TODO: This test case is messed up (order of the tests matters, tests extends junit 3 but uses testng)
public class ChannelSegmentFilterApplierTest extends TestCase {
    private ChannelEntity cE1;
    private ChannelEntity cE2;
    private ChannelEntity cE3;
    private ChannelFeedbackEntity cFE1;
    private ChannelFeedbackEntity cFE2;
    private ChannelFeedbackEntity cFE3;
    private ChannelSegmentFeedbackEntity cSFE1;
    private ChannelSegmentFeedbackEntity cSFE2;
    private ChannelSegmentFeedbackEntity cSFE3;
    private ChannelSegmentFeedbackEntity cSFE4;
    private ChannelSegmentFeedbackEntity cSFE5;
    private ChannelSegmentFeedbackEntity cSFE6;
    private ChannelSegmentEntity channelSegmentEntity1;
    private ChannelSegmentEntity channelSegmentEntity2;
    private ChannelSegmentEntity channelSegmentEntity3;
    private ChannelSegmentEntity channelSegmentEntity4;
    private ChannelSegmentEntity channelSegmentEntity5;
    private ChannelSegmentEntity channelSegmentEntity6;
    private ChannelSegment channelSegment1;
    private ChannelSegment channelSegment2;
    private ChannelSegment channelSegment3;
    private ChannelSegment channelSegment4;
    private ChannelSegment channelSegment5;
    private ChannelSegment channelSegment6;
    private List<AdvertiserLevelFilter> dcpAndRtbdAdvertiserLevelFilters;
    private List<AdGroupLevelFilter> dcpAndRtbAdGroupLevelFilters;
    private Set<String> emptySet;
    private Set<String> emptySet2;
    private RepositoryHelper repositoryHelper;
    private SiteMetaDataEntity sMDE;
    private ChannelSegmentEntity s1;
    private ChannelSegmentEntity s2;
    private SASRequestParameters sasParams;
    private ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private Injector injector;
    private String advertiserId1;
    private String advertiserId2;
    private String advertiserId3;
    private ConfigurationLoader configurationLoder;

    @Override
    public void setUp() throws Exception {
        configurationLoder = ConfigurationLoader.getInstance("channel-server.properties");
        CasConfigUtil.init(configurationLoder, null);
        emptySet = new HashSet<>();
        emptySet2 = new HashSet<>();

        advertiserId1 = "4028cb1e38411ed20138515af2b6025a";
        advertiserId2 = "4028cbe0373b25ce0137c0eb0c091e83";
        advertiserId3 = "72cd0cbf-a1eb-4905-b6d5-de28acb72dc8";

        final ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setAccountId(advertiserId1);
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("tapit");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(false);
        cE1Builder.setSitesIE(emptySet);
        cE1 = cE1Builder.build();

        final ChannelEntity.Builder cE2Builder = ChannelEntity.newBuilder();
        cE2Builder.setAccountId(advertiserId2);
        cE2Builder.setPriority(1);
        cE2Builder.setImpressionCeil(90);
        cE2Builder.setName("openx");
        cE2Builder.setRequestCap(100);
        cE2Builder.setSiteInclusion(false);
        cE2Builder.setSitesIE(emptySet);
        cE2 = cE2Builder.build();

        final ChannelEntity.Builder cE3Builder = ChannelEntity.newBuilder();
        cE3Builder.setAccountId(advertiserId3);
        cE3Builder.setPriority(5);
        cE3Builder.setImpressionCeil(90);
        cE3Builder.setName("nexage");
        cE3Builder.setRequestCap(100);
        cE3Builder.setSiteInclusion(false);
        cE3Builder.setSitesIE(emptySet);
        cE3 = cE3Builder.build();

        cFE1 =
                new ChannelFeedbackEntity(getChannelFeedbackEntityBuilder(advertiserId1, 100.0, 50.0, 50.0, 100, 95, 120, 1, 4.0));
        cFE2 =
                new ChannelFeedbackEntity(getChannelFeedbackEntityBuilder(advertiserId2, 100.0, 95.0, 5.0, 100, 55, 120, 2, 0.6));
        cFE3 =
                new ChannelFeedbackEntity(getChannelFeedbackEntityBuilder(advertiserId2, 100.0, 50.0, 50.0, 100, 55, 0, 1, 4.0));
        cSFE1 =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(advertiserId1, "adgroupId1", 0.29, 0.1, 0, 0, 0, 0, 200));
        cSFE2 =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(advertiserId1, "adgroupId2", 0.9, 0.1, 0, 0, 0, 0, 0));
        cSFE3 =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(advertiserId1, "adgroupId3", 0.4, 0.1, 0, 0, 0, 0, 0));
        cSFE4 =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(advertiserId2, "adgroupId4", 0.2, 0.1, 0, 0, 0, 0, 50));
        cSFE5 =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(advertiserId2, "adgroupId5", 0.5, 0.1, 0, 0, 0, 0, 0));
        cSFE6 =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(advertiserId3, "adgroupId6", 0.7, 0.1, 0, 0, 0, 0, 0));
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        channelSegmentEntity1 =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(advertiserId1, "adgroupId1", "adId", "channelId1", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d, null, null, true, emptySet, 100));
        channelSegmentEntity2 =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(advertiserId1, "adgroupId2", "adId", "channelId1", 0, rcList, tags, false, true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        channelSegmentEntity3 =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(advertiserId1, "adgroupId3", "adId", "channelId1", 1, rcList, tags, false, false, "externalSiteKey", modified_on, "campaignId", slotIds, 0, false, "pricingModel", siteRatings, 0, null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        channelSegmentEntity4 =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(advertiserId2, "adgroupId4", "adId", "channelId2", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 100));
        channelSegmentEntity5 =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(advertiserId2, "adgroupId5", "adId", "channelId2", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        channelSegmentEntity6 =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(advertiserId3, "adgroupId5", "adId", "channelId3", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));

        final ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity =
                new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder(null, null, 2.1, 60, 12, 123, 12, 11, 0));
        channelSegment1 =
                new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, channelSegmentFeedbackEntity, null, cSFE1
                        .getECPM());
        channelSegment2 =
                new ChannelSegment(channelSegmentEntity2, cE1, cFE1, cSFE2, channelSegmentFeedbackEntity, null, cSFE2
                        .getECPM());
        channelSegment3 =
                new ChannelSegment(channelSegmentEntity3, cE1, cFE1, cSFE3, channelSegmentFeedbackEntity, null, cSFE3
                        .getECPM());
        channelSegment4 =
                new ChannelSegment(channelSegmentEntity4, cE2, cFE2, cSFE4, channelSegmentFeedbackEntity, null, cSFE4
                        .getECPM());
        channelSegment5 =
                new ChannelSegment(channelSegmentEntity5, cE2, cFE2, cSFE5, channelSegmentFeedbackEntity, null, cSFE5
                        .getECPM());
        channelSegment6 =
                new ChannelSegment(channelSegmentEntity6, cE3, cFE3, cSFE6, channelSegmentFeedbackEntity, null, cSFE6
                        .getECPM());

        sMDE = createMock(SiteMetaDataEntity.class);
        expect(sMDE.getAdvertisersIncludedBySite()).andReturn(emptySet).anyTimes();
        expect(sMDE.getAdvertisersIncludedByPublisher()).andReturn(emptySet2).anyTimes();
        replay(sMDE);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySiteMetaDetaRepository("siteid")).andReturn(sMDE).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 1)).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        replay(repositoryHelper);

        s1 = createMock(ChannelSegmentEntity.class);
        expect(s1.isUdIdRequired()).andReturn(false).anyTimes();
        expect(s1.isZipCodeRequired()).andReturn(false).anyTimes();
        expect(s1.isLatlongRequired()).andReturn(false).anyTimes();
        expect(s1.isRestrictedToRichMediaOnly()).andReturn(false).anyTimes();
        expect(s1.isInterstitialOnly()).andReturn(false).anyTimes();
        expect(s1.isNonInterstitialOnly()).andReturn(false).anyTimes();
        expect(s1.getAdgroupId()).andReturn("").anyTimes();
        expect(s1.getAdvertiserId()).andReturn("").anyTimes();
        replay(s1);
        s2 = createMock(ChannelSegmentEntity.class);
        expect(s2.isUdIdRequired()).andReturn(true).anyTimes();
        expect(s2.isZipCodeRequired()).andReturn(true).anyTimes();
        expect(s2.isLatlongRequired()).andReturn(true).anyTimes();
        expect(s2.isRestrictedToRichMediaOnly()).andReturn(true).anyTimes();
        expect(s2.isInterstitialOnly()).andReturn(true).anyTimes();
        expect(s2.isNonInterstitialOnly()).andReturn(false).anyTimes();
        expect(s2.getAdvertiserId()).andReturn(advertiserId1).anyTimes();
        expect(s2.getAdgroupId()).andReturn("").anyTimes();
        replay(s2);
        sasParams = new SASRequestParameters();
        sasParams.setPostalCode("110051");
        sasParams.setLatLong("11.35&12.56");
        sasParams.setRichMedia(true);
        sasParams.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        sasParams.setSiteId("siteid");
        Map<String, String> tUidParam = new HashedMap();
        tUidParam.put("O1", "O1_user_id");
        sasParams.setTUidParams(tUidParam);


        injector = Guice.createInjector(Modules
                .override(new ServerModule(configurationLoder, repositoryHelper, "containerName"), new CasNettyModule(configurationLoder
                        .getServerConfiguration())).with(new TestScopeModule()));

        channelSegmentFilterApplier = injector.getInstance(ChannelSegmentFilterApplier.class);

        final TypeLiteral<List<AdvertiserLevelFilter>> advertiserLevelTypeLiteral =
                new TypeLiteral<List<AdvertiserLevelFilter>>() {
                };
        final Key<List<AdvertiserLevelFilter>> dcpAndRtbdAdvertiserLevelFiltersKey =
                Key.get(advertiserLevelTypeLiteral, DcpAndRtbdAdvertiserLevelFilters.class);
        dcpAndRtbdAdvertiserLevelFilters = injector.getInstance(dcpAndRtbdAdvertiserLevelFiltersKey);

        final TypeLiteral<List<AdGroupLevelFilter>> adGroupLevelTypeLiteral =
                new TypeLiteral<List<AdGroupLevelFilter>>() {
                };
        final Key<List<AdGroupLevelFilter>> dcpAndRtbAdGroupLevelFiltersKey =
                Key.get(adGroupLevelTypeLiteral, DcpAndRtbAdGroupLevelFilters.class);
        dcpAndRtbAdGroupLevelFilters = injector.getInstance(dcpAndRtbAdGroupLevelFiltersKey);

    }

    @Test
    public void testIsBurnLimitExceeded() {
        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails = Lists.newArrayList();
        advertiserMatchedSegmentDetails.add(new AdvertiserMatchedSegmentDetail(Lists
                .newArrayList(channelSegment1, channelSegment4, channelSegment6)));

        final List<ChannelSegment> channelSegments = channelSegmentFilterApplier
                .getChannelSegments(advertiserMatchedSegmentDetails, sasParams, new CasContext(), dcpAndRtbdAdvertiserLevelFilters, dcpAndRtbAdGroupLevelFilters);

        assertEquals(true, channelSegments.contains(channelSegment1));
        assertEquals(true, channelSegments.contains(channelSegment4));
        assertEquals(true, channelSegments.contains(channelSegment6));
    }

    @Test
    public void testIsDailyImpressionCeilingExceeded() {
        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails = Lists.newArrayList();
        advertiserMatchedSegmentDetails.add(new AdvertiserMatchedSegmentDetail(Lists
                .newArrayList(channelSegment1, channelSegment4, channelSegment6)));

        final List<ChannelSegment> channelSegments = channelSegmentFilterApplier
                .getChannelSegments(advertiserMatchedSegmentDetails, sasParams, new CasContext(), dcpAndRtbdAdvertiserLevelFilters, dcpAndRtbAdGroupLevelFilters);

        assertEquals(true, channelSegments.contains(channelSegment1));
        assertEquals(true, channelSegments.contains(channelSegment4));
        assertEquals(true, channelSegments.contains(channelSegment6));
    }

    @Test
    public void testIsDailyRequestCapExceeded() {
        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails = Lists.newArrayList();
        advertiserMatchedSegmentDetails.add(new AdvertiserMatchedSegmentDetail(Lists
                .newArrayList(channelSegment1, channelSegment4, channelSegment6)));

        final List<ChannelSegment> channelSegments = channelSegmentFilterApplier
                .getChannelSegments(advertiserMatchedSegmentDetails, sasParams, new CasContext(), dcpAndRtbdAdvertiserLevelFilters, dcpAndRtbAdGroupLevelFilters);
        assertEquals(true, channelSegments.contains(channelSegment1));
        assertEquals(true, channelSegments.contains(channelSegment4));
        assertEquals(true, channelSegments.contains(channelSegment6));
    }

    @Test
    public void testAdvertiserLevelFilter() {
        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails = Lists.newArrayList();
        advertiserMatchedSegmentDetails.add(new AdvertiserMatchedSegmentDetail(Lists
                .newArrayList(channelSegment1, channelSegment2, channelSegment3)));
        advertiserMatchedSegmentDetails
                .add(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment4, channelSegment5)));
        advertiserMatchedSegmentDetails.add(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment6)));

        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSiteId("siteid");


        for (final AdvertiserLevelFilter advertiserLevelFilter : dcpAndRtbdAdvertiserLevelFilters) {
            advertiserLevelFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        }
        final List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);

        assertEquals(6, channelSegmentList.size());
        assertEquals(false, channelSegmentList.get(0).getChannelEntity().getAccountId()
                .equals(channelSegmentEntity6.getAdvertiserId()));
    }

    private List<ChannelSegment> getChannelSegments(
            final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails) {
        final List<ChannelSegment> channelSegmentList = Lists.newArrayList();

        for (final AdvertiserMatchedSegmentDetail matchedSegmentDetail : advertiserMatchedSegmentDetails) {
            channelSegmentList.addAll(matchedSegmentDetail.getChannelSegmentList());
        }
        return channelSegmentList;
    }

    @Test
    public void testIsAnySegmentPropertyViolatedWhenNosegmentFlag() {
        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s1, null, null, cSFE1, null, null, 0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);

        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(true, channelSegments.contains(channelSegment));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedWhenUdIdFlagSet() {
        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s2, null, null, cSFE2, null, null, 0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);

        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(true, channelSegments.contains(channelSegment));

        sasParams.setTUidParams(new HashMap<>());
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(false, channelSegments.contains(channelSegment));

        sasParams.setTUidParams(null);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(false, channelSegments.contains(channelSegment));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedWhenZipCodeFlagSet() {

        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s2, null, null, cSFE2, null, null, 0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);

        sasParams.setPostalCode(null);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(false, channelSegments.contains(channelSegment));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedLatlongFlagSet() {

        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s2, null, null, cSFE2, null, null, 0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);

        sasParams.setLatLong(null);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(false, channelSegments.contains(channelSegment));

    }

    @Test
    public void testIsAnySegmentPropertyViolatedRichMediaFlagSet() {

        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s2, null, null, cSFE2, null, null, 0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);

        sasParams.setRichMedia(false);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(false, channelSegments.contains(channelSegment));

    }

    @Test
    public void testIsAnySegmentPropertyViolatedInterstitialFlagSet() {

        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s2, null, null, cSFE2, null, null, 0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(true, channelSegments.contains(channelSegment));

        sasParams.setRequestedAdType(null);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(false, channelSegments.contains(channelSegment));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedNonInterstitialFlagSet() {

        s2 = createMock(ChannelSegmentEntity.class);
        expect(s2.isUdIdRequired()).andReturn(true).anyTimes();
        expect(s2.isZipCodeRequired()).andReturn(true).anyTimes();
        expect(s2.isLatlongRequired()).andReturn(true).anyTimes();
        expect(s2.isRestrictedToRichMediaOnly()).andReturn(true).anyTimes();
        expect(s2.isInterstitialOnly()).andReturn(false).anyTimes();
        expect(s2.isNonInterstitialOnly()).andReturn(true).anyTimes();
        expect(s2.getAdvertiserId()).andReturn(advertiserId1).anyTimes();
        expect(s2.getAdgroupId()).andReturn("").anyTimes();
        replay(s2);

        final AdGroupPropertyViolationFilter adGroupPropertyViolationFilter =
                injector.getInstance(AdGroupPropertyViolationFilter.class);

        final ChannelSegment channelSegment = new ChannelSegment(s2, null, null, cSFE2, null, null, 0);
        List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(false, channelSegments.contains(channelSegment));

        sasParams.setRequestedAdType(null);
        channelSegments = Lists.newArrayList(channelSegment);
        adGroupPropertyViolationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(true, channelSegments.contains(channelSegment));

    }

    @Test
    public void testAdGroupLevelFiltering() {

        final List<ChannelSegment> channelSegments =
                Lists.newArrayList(channelSegment1, channelSegment2, channelSegment3, channelSegment4, channelSegment5, channelSegment6);

        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSiteFloor(0.3);
        sasParams.setCountryId((long) 1);
        sasParams.setOsId(1);
        sasParams.setDst(2);
        sasParams.setSiteId("siteid");

        final CasContext casContext = new CasContext();
        int sumOfSiteImpressions = 0;
        for (final ChannelSegment channelSegment : channelSegments) {
            sumOfSiteImpressions += channelSegment.getChannelSegmentAerospikeFeedbackEntity().getBeacons();
        }
        casContext.setSumOfSiteImpressions(sumOfSiteImpressions);

        for (final AdGroupLevelFilter adGroupLevelFilter : dcpAndRtbAdGroupLevelFilters) {
            adGroupLevelFilter.filter(channelSegments, sasParams, casContext);
        }

        int countAdvertiserId1 = 0;
        int countAdvertiserId2 = 0;
        int countAdvertiserId3 = 0;

        for (final ChannelSegment channelSegment : channelSegments) {

            if (channelSegment.getChannelEntity().getAccountId().equals(advertiserId1)) {
                countAdvertiserId1++;
            } else if (channelSegment.getChannelEntity().getAccountId().equals(advertiserId2)) {
                countAdvertiserId2++;
            } else if (channelSegment.getChannelEntity().getAccountId().equals(advertiserId3)) {
                countAdvertiserId3++;
            }

        }

        assertEquals(3, countAdvertiserId1);
        assertEquals(2, countAdvertiserId2);
        assertEquals(1, countAdvertiserId3);
        assertEquals(true, channelSegments.contains(channelSegment1));
        assertEquals(true, channelSegments.contains(channelSegment4));
    }

    /*@Test
    public void testSelectTopAdgroupsForRequest() {
        final List<ChannelSegment> channelSegments =
                Lists.newArrayList(channelSegment1, channelSegment2, channelSegment3, channelSegment4, channelSegment5, channelSegment6);

        final AdGroupMaxSegmentPerRequestFilter adGroupTotalCountFilter =
                injector.getInstance(AdGroupMaxSegmentPerRequestFilter.class);
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(5, channelSegments.size());
    }*/

    @Test
    public void testIsAdvertiserExcludedWhenSiteInclusionListEmptyPublisherInclusionListEmpty() {
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);

        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));

        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        final List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);

        assertEquals(true, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsAdvertiserExcludedWhenSiteInclusionListEmpty() {
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);

        emptySet2.add("123");
        List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(false, channelSegmentList.contains(channelSegment1));

        emptySet2.add(advertiserId1);
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));

        emptySet2.clear();
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsAdvertiserExcludedWhenPublisherInclusionListEmpty() {
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);

        emptySet.add("123");
        List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(false, channelSegmentList.contains(channelSegment1));

        emptySet.add(advertiserId1);
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));

        emptySet.clear();
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsAdvertiserExcludedWhenSiteInclusionListNotEmptyPublisherInclusionListNotEmpty() {
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);

        List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));

        emptySet.add("123");
        emptySet2.add(advertiserId1);
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(false, channelSegmentList.contains(channelSegment1));

        emptySet.add(advertiserId1);
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));

        emptySet.clear();
        emptySet2.remove(advertiserId1);
        emptySet2.add("123");
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(false, channelSegmentList.contains(channelSegment1));

    }

    @Test
    public void testIsSiteExcludedByAdvertiserInclusionTrueEmptyList() {
        final ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId(advertiserId1);
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(true);
        cE1Builder.setSitesIE(emptySet);
        cE1 = cE1Builder.build();
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null, null, cSFE1.getECPM());

        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);
        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        final List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);

        assertEquals(true, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserInclusionTrueNonEmptyList() {
        final ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId(advertiserId1);
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(true);
        cE1Builder.setSitesIE(emptySet);
        cE1 = cE1Builder.build();
        emptySet.add("siteid1");
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null, null, cSFE1.getECPM());
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);
        List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));

        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(false, channelSegmentList.contains(channelSegment1));

        emptySet.add(advertiserId1);
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserExclusionTrueEmptyList() {
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);
        final List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));

        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        final List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserExclusionTrueNonEmptyList() {
        emptySet.add(advertiserId1);
        final AdvertiserExcludedFilter advertiserExcludedFilter = injector.getInstance(AdvertiserExcludedFilter.class);
        List<AdvertiserMatchedSegmentDetail> advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));

        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        List<ChannelSegment> channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(true, channelSegmentList.contains(channelSegment1));

        emptySet.clear();
        emptySet.add("siteid");
        advertiserMatchedSegmentDetails =
                Lists.newArrayList(new AdvertiserMatchedSegmentDetail(Lists.newArrayList(channelSegment1)));
        advertiserExcludedFilter.filter(advertiserMatchedSegmentDetails, sasParams);
        channelSegmentList = getChannelSegments(advertiserMatchedSegmentDetails);
        assertEquals(false, channelSegmentList.contains(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdGroupInclusionTrueEmptyList() {
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);

        final AdGroupSiteExclusionFilter adGroupTotalCountFilter =
                injector.getInstance(AdGroupSiteExclusionFilter.class);
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdGroupInclusionTrueNonEmptyList() {
        final ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId(advertiserId1);
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(true);
        cE1Builder.setSitesIE(emptySet);
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null, null, cSFE1.getECPM());
        cE1 = cE1Builder.build();

        final AdGroupSiteExclusionFilter adGroupTotalCountFilter =
                injector.getInstance(AdGroupSiteExclusionFilter.class);

        List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);
        emptySet.add(advertiserId1);
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(false, channelSegments.contains(channelSegment1));

        emptySet.clear();
        emptySet.add("siteid");
        channelSegments = Lists.newArrayList(channelSegment1);
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdGroupExclusionTrueEmptyList() {
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment2);

        final AdGroupSiteExclusionFilter adGroupTotalCountFilter =
                injector.getInstance(AdGroupSiteExclusionFilter.class);
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(true, channelSegments.contains(channelSegment2));
    }

    @Test
    public void testIsSiteExcludedByAdGroupExclusionTrueNonEmptyList() {

        final AdGroupSiteExclusionFilter adGroupTotalCountFilter =
                injector.getInstance(AdGroupSiteExclusionFilter.class);

        List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment2);
        emptySet.add(advertiserId1);
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(true, channelSegments.contains(channelSegment2));

        channelSegments = Lists.newArrayList(channelSegment2);
        emptySet.clear();
        emptySet.add("siteid");
        adGroupTotalCountFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(false, channelSegments.contains(channelSegment2));
    }

    @Test
    public void testIsAdGroupDailyImpressionCeilingExceeded() {
        final AdGroupDailyImpressionCountFilter adGroupDailyImpressionCountFilter =
                injector.getInstance(AdGroupDailyImpressionCountFilter.class);

        final List<ChannelSegment> channelSegments =
                Lists.newArrayList(channelSegment1, channelSegment4, channelSegment6);
        adGroupDailyImpressionCountFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(false, channelSegments.contains(channelSegment1));
        assertEquals(true, channelSegments.contains(channelSegment4));
        assertEquals(true, channelSegments.contains(channelSegment6));
    }

    @Test
    public void testIsDemandAcceptedBySupplyWithDefaultSupplyClassDefaultDemandClass() {
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 2)).andReturn(null).anyTimes();
        replay(repositoryHelper);

        sasParams.setCountryId((long) 1);
        sasParams.setOsId(2);

        final Injector injector = Guice.createInjector(Modules
                .override(new ServerModule(configurationLoder, repositoryHelper, "containerName"), new CasNettyModule(configurationLoder
                        .getServerConfiguration())).with(new TestScopeModule()));

        final AdGroupSupplyDemandClassificationFilter adGroupSupplyDemandClassificationFilter =
                injector.getInstance(AdGroupSupplyDemandClassificationFilter.class);

        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);

        adGroupSupplyDemandClassificationFilter.filter(channelSegments, sasParams, new CasContext());
        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    @Test
    public void testIsDemandAcceptedBySupplyWithDefaultDemandClass() {

        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 2)).andReturn(null).anyTimes();
        replay(repositoryHelper);

        sasParams.setSiteId("siteid");
        sasParams.setCountryId((long) 1);
        sasParams.setOsId(2);

        final Injector injector = Guice.createInjector(Modules
                .override(new ServerModule(configurationLoder, repositoryHelper, "containerName"), new CasNettyModule(configurationLoder
                        .getServerConfiguration())).with(new TestScopeModule()));

        final AdGroupSupplyDemandClassificationFilter adGroupSupplyDemandClassificationFilter =
                injector.getInstance(AdGroupSupplyDemandClassificationFilter.class);

        channelSegment1.setPrioritisedECPM(3.0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);

        adGroupSupplyDemandClassificationFilter.filter(channelSegments, sasParams, new CasContext());

        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsDemandAcceptedBySupplyWithDefaultSupplyClass() {

        final PricingEngineEntity.Builder builder = PricingEngineEntity.newBuilder();
        builder.setCountryId(1);
        builder.setOsId(2);
        builder.setSupplyToDemandMap(new HashedMap(ImmutableMap.of("1", ImmutableSet.of("2"))));
        final PricingEngineEntity pricingEngineEntity = builder.build();

        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(pricingEngineEntity).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 2)).andReturn(null).anyTimes();
        replay(repositoryHelper);

        sasParams.setSiteId("siteid");
        sasParams.setCountryId((long) 1);
        sasParams.setOsId(2);

        final Injector injector = Guice.createInjector(Modules
                .override(new ServerModule(configurationLoder, repositoryHelper, "containerName"), new CasNettyModule(configurationLoder
                        .getServerConfiguration())).with(new TestScopeModule()));

        final AdGroupSupplyDemandClassificationFilter adGroupSupplyDemandClassificationFilter =
                injector.getInstance(AdGroupSupplyDemandClassificationFilter.class);

        channelSegment1.setPrioritisedECPM(3.0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);

        final CasContext casContext = new CasContext();

        casContext.setPricingEngineEntity(injector.getInstance(CasUtils.class).fetchPricingEngineEntity(sasParams));
        adGroupSupplyDemandClassificationFilter.filter(channelSegments, sasParams, casContext);

        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsDemandAcceptedBySupplyPass() {
        final SiteEcpmEntity.Builder builder1 = SiteEcpmEntity.newBuilder();
        builder1.siteId("siteid");
        builder1.countryId(1);
        builder1.osId(1);
        builder1.ecpm(3.0);
        builder1.networkEcpm(1.0);
        builder1.build();

        final PricingEngineEntity.Builder builder2 = PricingEngineEntity.newBuilder();
        builder2.setCountryId(1);
        builder2.setOsId(2);
        builder2.setSupplyToDemandMap(new HashedMap(ImmutableMap.of("0", ImmutableSet.of("0", "1"))));
        final PricingEngineEntity pricingEngineEntity = builder2.build();

        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(pricingEngineEntity).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 2)).andReturn(null).anyTimes();
        replay(repositoryHelper);

        final Injector injector = Guice.createInjector(Modules
                .override(new ServerModule(configurationLoder, repositoryHelper, "containerName"), new CasNettyModule(configurationLoder
                        .getServerConfiguration())).with(new TestScopeModule()));

        sasParams.setSiteId("siteid");
        sasParams.setCountryId((long) 1);
        sasParams.setOsId(2);

        final CasContext casContext = new CasContext();
        casContext.setPricingEngineEntity(injector.getInstance(CasUtils.class).fetchPricingEngineEntity(sasParams));

        channelSegment1.setPrioritisedECPM(3.0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);
        final AdGroupSupplyDemandClassificationFilter adGroupSupplyDemandClassificationFilter =
                injector.getInstance(AdGroupSupplyDemandClassificationFilter.class);
        adGroupSupplyDemandClassificationFilter.filter(channelSegments, sasParams, casContext);

        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsDemandAcceptedBySupplyFail() {
        final SiteEcpmEntity.Builder builder1 = SiteEcpmEntity.newBuilder();
        builder1.siteId("siteid");
        builder1.countryId(1);
        builder1.osId(1);
        builder1.ecpm(3.0);
        builder1.networkEcpm(1.0);
        builder1.build();

        final PricingEngineEntity.Builder builder2 = PricingEngineEntity.newBuilder();
        builder2.setCountryId(1);
        builder2.setOsId(2);
        builder2.setSupplyToDemandMap(new HashedMap(ImmutableMap.of("0", ImmutableSet.of("1"))));
        final PricingEngineEntity pricingEngineEntity = builder2.build();

        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(pricingEngineEntity).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 2)).andReturn(null).anyTimes();
        replay(repositoryHelper);

        sasParams.setSiteId("siteid");
        sasParams.setCountryId((long) 1);
        sasParams.setOsId(2);

        final Injector injector = Guice.createInjector(Modules
                .override(new ServerModule(configurationLoder, repositoryHelper, "containerName"), new CasNettyModule(configurationLoder
                        .getServerConfiguration())).with(new TestScopeModule()));

        final CasContext casContext = new CasContext();
        casContext.setPricingEngineEntity(injector.getInstance(CasUtils.class).fetchPricingEngineEntity(sasParams));
        channelSegment1.setPrioritisedECPM(3.0);
        final List<ChannelSegment> channelSegments = Lists.newArrayList(channelSegment1);
        final AdGroupSupplyDemandClassificationFilter adGroupSupplyDemandClassificationFilter =
                injector.getInstance(AdGroupSupplyDemandClassificationFilter.class);
        adGroupSupplyDemandClassificationFilter.filter(channelSegments, sasParams, casContext);

        assertEquals(true, channelSegments.contains(channelSegment1));
    }

    public static ChannelSegmentEntity.Builder getChannelSegmentEntityBuilder(final String advertiserId,
            final String adgroupId, final String adId, final String channelId, final long platformTargeting,
            final Long[] rcList, final Long[] tags, final boolean status, final boolean isTestMode,
            final String externalSiteKey, final Timestamp modified_on, final String campaignId, final Long[] slotIds,
            final long incId, final boolean allTags, final String pricingModel, final Integer[] siteRatings,
            final int targetingPlatform, final ArrayList<Integer> osIds, final boolean udIdRequired,
            final boolean zipCodeRequired, final boolean latlongRequired, final boolean richMediaOnly,
            final boolean appUrlEnabled, final boolean interstitialOnly, final boolean nonInterstitialOnly,
            final boolean stripUdId, final boolean stripZipCode, final boolean stripLatlong,
            final JSONObject additionalParams, final List<Long> manufModelTargetingList, final double ecpmBoost,
            final Timestamp eCPMBoostDate, final Long[] tod, final boolean siteInclusion, final Set<String> siteIE,
            final int impressionCeil) {
        final ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setAdvertiserId(advertiserId);
        builder.setAdgroupId(adgroupId);
        builder.setAdIds(new String[] {adId});
        builder.setChannelId(channelId);
        builder.setPlatformTargeting(platformTargeting);
        builder.setRcList(rcList);
        builder.setTags(tags);
        builder.setCategoryTaxonomy(tags);
        builder.setAllTags(allTags);
        builder.setStatus(status);
        builder.setTestMode(isTestMode);
        builder.setExternalSiteKey(externalSiteKey);
        builder.setModified_on(modified_on);
        builder.setCampaignId(campaignId);
        builder.setSlotIds(slotIds);
        builder.setIncIds(new Long[] {incId});
        builder.setAdgroupIncId(incId);
        builder.setPricingModel(pricingModel);
        builder.setSiteRatings(siteRatings);
        builder.setTargetingPlatform(targetingPlatform);
        builder.setOsIds(osIds);
        builder.setUdIdRequired(udIdRequired);
        builder.setLatlongRequired(latlongRequired);
        builder.setZipCodeRequired(zipCodeRequired);
        builder.setRestrictedToRichMediaOnly(richMediaOnly);
        builder.setAppUrlEnabled(appUrlEnabled);
        builder.setInterstitialOnly(interstitialOnly);
        builder.setNonInterstitialOnly(nonInterstitialOnly);
        builder.setStripUdId(stripUdId);
        builder.setStripLatlong(stripLatlong);
        builder.setStripZipCode(stripZipCode);
        builder.setAdditionalParams(additionalParams);
        builder.setManufModelTargetingList(manufModelTargetingList);
        builder.setEcpmBoost(ecpmBoost);
        builder.setEcpmBoostExpiryDate(eCPMBoostDate);
        builder.setTod(tod);
        builder.setSiteInclusion(siteInclusion);
        builder.setSitesIE(siteIE);
        builder.setImpressionCeil(impressionCeil);
        return builder;
    }

    private ChannelFeedbackEntity.Builder getChannelFeedbackEntityBuilder(final String advertiserId,
            final double totalInflow, final double totalBurn, final double balance, final int totalImpressions,
            final int todayImpressions, final int todayRequests, final int averageLatency, final double revenue) {
        final ChannelFeedbackEntity.Builder builder = ChannelFeedbackEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setTotalInflow(totalInflow);
        builder.setTotalBurn(totalBurn);
        builder.setBalance(balance);
        builder.setAverageLatency(averageLatency);
        builder.setTotalImpressions(totalImpressions);
        builder.setTodayImpressions(todayImpressions);
        builder.setTodayRequests(todayRequests);
        builder.setRevenue(revenue);
        return builder;
    }

    private ChannelSegmentFeedbackEntity.Builder getChannelSegmentFeedbackBuilder(final String advertiserId,
            final String adGroupId, final double eCPM, final double fillRatio, final double latency, final int requests,
            final int beacons, final int clicks, final int todaysImpressions) {
        final ChannelSegmentFeedbackEntity.Builder builder = ChannelSegmentFeedbackEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setAdGroupId(adGroupId);
        builder.setECPM(eCPM);
        builder.setFillRatio(fillRatio);
        builder.setLastHourLatency(latency);
        builder.setTodayRequests(requests);
        builder.setBeacons(beacons);
        builder.setClicks(clicks);
        builder.setTodayImpressions(todaysImpressions);
        return builder;
    }
}
