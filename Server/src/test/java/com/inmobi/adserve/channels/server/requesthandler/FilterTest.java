package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.slf4j.Marker;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.util.ConfigurationLoader;


public class FilterTest extends TestCase {
    Configuration                        mockConfig;
    Configuration                        mockAdapterConfig;
    private ChannelEntity                cE1;
    private ChannelEntity                cE2;
    private ChannelEntity                cE3;
    private ChannelFeedbackEntity        cFE1;
    private ChannelFeedbackEntity        cFE2;
    private ChannelFeedbackEntity        cFE3;
    private ChannelSegmentFeedbackEntity cSFE1;
    private ChannelSegmentFeedbackEntity cSFE2;
    private ChannelSegmentFeedbackEntity cSFE3;
    private ChannelSegmentFeedbackEntity cSFE4;
    private ChannelSegmentFeedbackEntity cSFE5;
    private ChannelSegmentFeedbackEntity cSFE6;
    private ChannelSegmentEntity         channelSegmentEntity1;
    private ChannelSegmentEntity         channelSegmentEntity2;
    private ChannelSegmentEntity         channelSegmentEntity3;
    private ChannelSegmentEntity         channelSegmentEntity4;
    private ChannelSegmentEntity         channelSegmentEntity5;
    private ChannelSegmentEntity         channelSegmentEntity6;
    private ChannelSegment               channelSegment1;
    private ChannelSegment               channelSegment2;
    private ChannelSegment               channelSegment3;
    private ChannelSegment               channelSegment4;
    private ChannelSegment               channelSegment5;
    private ChannelSegment               channelSegment6;
    private Set<String>                  emptySet;
    private Set<String>                  emptySet2;
    private RepositoryHelper             repositoryHelper;
    private SiteMetaDataEntity           sMDE;
    private ChannelSegmentEntity         s1;
    private ChannelSegmentEntity         s2;
    private SASRequestParameters         sasParams;

    private static void setPrivateStatic(final String fieldName, final Object newValue) throws Exception {
        Field field = Filters.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }

    @Override
    public void setUp() throws Exception {

        setPrivateStatic("traceMarkerProvider", new Provider<Marker>() {
            @Override
            public Marker get() {
                return null;
            }
        });

        ConfigurationLoader config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
        ServletHandler.init(config, null);
        mockConfig = createMock(Configuration.class);
        mockAdapterConfig = createMock(Configuration.class);
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put("openx.advertiserId", "");
        temp.put("openx.partnerSegmentNo", "");
        temp.put("atnt.advertiserId", "");
        temp.put("atnt.partnerSegmentNo", "");
        temp.put("tapit.advertiserId", "");
        temp.put("tapit.partnerSegmentNo", "");
        temp.put("mullahmedia.advertiserId", "");
        Iterator<String> itr = temp.keySet().iterator();
        emptySet = new HashSet<String>();
        emptySet2 = new HashSet<String>();

        ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId("advertiserId1");
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(false);
        cE1Builder.setSitesIE(emptySet);
        cE1 = cE1Builder.build();

        ChannelEntity.Builder cE2Builder = ChannelEntity.newBuilder();
        cE2Builder.setChannelId("advertiserId2");
        cE2Builder.setPriority(1);
        cE2Builder.setImpressionCeil(90);
        cE2Builder.setName("name2");
        cE2Builder.setRequestCap(100);
        cE2Builder.setSiteInclusion(false);
        cE2Builder.setSitesIE(emptySet);
        cE2 = cE2Builder.build();

        ChannelEntity.Builder cE3Builder = ChannelEntity.newBuilder();
        cE3Builder.setChannelId("advertiserId3");
        cE3Builder.setPriority(5);
        cE3Builder.setImpressionCeil(90);
        cE3Builder.setName("name3");
        cE3Builder.setRequestCap(100);
        cE3Builder.setSiteInclusion(false);
        cE3Builder.setSitesIE(emptySet);
        cE3 = cE3Builder.build();

        cFE1 = new ChannelFeedbackEntity(getChannelFeedbackEntityBuilder("advertiserId1", 100.0, 50.0, 50.0, 100, 95,
            120, 1.0, 4.0));
        cFE2 = new ChannelFeedbackEntity(getChannelFeedbackEntityBuilder("advertiserId2", 100.0, 95.0, 5.0, 100, 55,
            120, 2.0, 0.6));
        cFE3 = new ChannelFeedbackEntity(getChannelFeedbackEntityBuilder("advertiserId2", 100.0, 50.0, 50.0, 100, 55,
            0, 1.0, 4.0));
        cSFE1 = new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder("advertiserId1", "adgroupId1", 0.29,
            0.1, 0, 0, 0, 0, 200));
        cSFE2 = new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder("advertiserId1", "adgroupId2", 0.9,
            0.1, 0, 0, 0, 0, 0));
        cSFE3 = new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder("advertiserId1", "adgroupId3", 0.4,
            0.1, 0, 0, 0, 0, 0));
        cSFE4 = new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder("advertiserId2", "adgroupId4", 0.2,
            0.1, 0, 0, 0, 0, 50));
        cSFE5 = new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder("advertiserId2", "adgroupId5", 0.5,
            0.1, 0, 0, 0, 0, 0));
        cSFE6 = new ChannelSegmentFeedbackEntity(getChannelSegmentFeedbackBuilder("advertiserId3", "adgroupId6", 0.7,
            0.1, 0, 0, 0, 0, 0));
        Long[] rcList = null;
        Long[] tags = null;
        Timestamp modified_on = null;
        Long[] slotIds = null;
        Integer[] siteRatings = null;
        channelSegmentEntity1 = new ChannelSegmentEntity(getChannelSegmentEntityBuilder("advertiserId1", "adgroupId1",
            "adId", "channelId1", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
            1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false,
            false, false, null, new ArrayList<Integer>(), 0.0d, null, null, true, emptySet, 100));
        channelSegmentEntity2 = new ChannelSegmentEntity(getChannelSegmentEntityBuilder("advertiserId1", "adgroupId2",
            "adId", "channelId1", 0, rcList, tags, false, true, "externalSiteKey", modified_on, "campaignId", slotIds,
            1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false,
            false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false, emptySet, 0));
        channelSegmentEntity3 = new ChannelSegmentEntity(getChannelSegmentEntityBuilder("advertiserId1", "adgroupId3",
            "adId", "channelId1", 1, rcList, tags, false, false, "externalSiteKey", modified_on, "campaignId", slotIds,
            0, false, "pricingModel", siteRatings, 0, null, false, false, false, false, false, false, false, false,
            false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false, emptySet, 0));
        channelSegmentEntity4 = new ChannelSegmentEntity(getChannelSegmentEntityBuilder("advertiserId2", "adgroupId4",
            "adId", "channelId2", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
            1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false,
            false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false, emptySet, 100));
        channelSegmentEntity5 = new ChannelSegmentEntity(getChannelSegmentEntityBuilder("advertiserId2", "adgroupId5",
            "adId", "channelId2", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
            1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false,
            false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false, emptySet, 0));
        channelSegmentEntity6 = new ChannelSegmentEntity(getChannelSegmentEntityBuilder("advertiserId3", "adgroupId5",
            "adId", "channelId3", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId", slotIds,
            1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false,
            false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false, emptySet, 0));

        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(
                getChannelSegmentFeedbackBuilder(null, null, 2.1, 60, 12, 123, 12, 11, 0));
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, channelSegmentFeedbackEntity,
                null, cSFE1.getECPM());
        channelSegment2 = new ChannelSegment(channelSegmentEntity2, cE1, cFE1, cSFE2, channelSegmentFeedbackEntity,
                null, cSFE2.getECPM());
        channelSegment3 = new ChannelSegment(channelSegmentEntity3, cE1, cFE1, cSFE3, channelSegmentFeedbackEntity,
                null, cSFE3.getECPM());
        channelSegment4 = new ChannelSegment(channelSegmentEntity4, cE2, cFE2, cSFE4, channelSegmentFeedbackEntity,
                null, cSFE4.getECPM());
        channelSegment5 = new ChannelSegment(channelSegmentEntity5, cE2, cFE2, cSFE5, channelSegmentFeedbackEntity,
                null, cSFE5.getECPM());
        channelSegment6 = new ChannelSegment(channelSegmentEntity6, cE3, cFE3, cSFE6, channelSegmentFeedbackEntity,
                null, cSFE6.getECPM());
        expect(mockAdapterConfig.getKeys()).andReturn(itr).anyTimes();
        expect(mockAdapterConfig.getString("openx.advertiserId")).andReturn("advertiserId1").anyTimes();
        expect(mockAdapterConfig.getString("atnt.advertiserId")).andReturn("advertiserId2").anyTimes();
        expect(mockAdapterConfig.getString("tapit.advertiserId")).andReturn("advertiserId3").anyTimes();
        expect(mockAdapterConfig.getBoolean("tapit.isRtb", false)).andReturn(false).anyTimes();
        expect(mockAdapterConfig.getString("mullahmedia.advertiserId")).andReturn("advertiserId4").anyTimes();
        expect(mockAdapterConfig.getInt("openx.partnerSegmentNo", 2)).andReturn(2).anyTimes();
        expect(mockAdapterConfig.getInt("atnt.partnerSegmentNo", 2)).andReturn(2).anyTimes();
        expect(mockAdapterConfig.getInt("tapit.partnerSegmentNo", 2)).andReturn(2).anyTimes();
        replay(mockAdapterConfig);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server" + ".properties");
        expect(mockConfig.getInt("totalSegmentNo", -1)).andReturn(5).anyTimes();
        expect(mockConfig.getDouble("revenueWindow", 0.33)).andReturn(10.0).anyTimes();
        expect(mockConfig.getDouble("ecpmShift", 0.1)).andReturn(0.0).anyTimes();
        expect(mockConfig.getDouble("feedbackPower", 2.0)).andReturn(1.0).anyTimes();
        expect(mockConfig.getInt("partnerSegmentNo", 2)).andReturn(2).anyTimes();
        expect(mockConfig.getInt("whiteListedSitesRefreshtime", 1000 * 300)).andReturn(0).anyTimes();
        expect(mockConfig.getInt("rtbBalanceFilterAmount", 50)).andReturn(0).anyTimes();
        expect(mockConfig.getDouble("normalizingFactor", 0.1)).andReturn(2.0).anyTimes();
        expect(mockConfig.getInt("defaultSupplyClass", 9)).andReturn(9).anyTimes();
        expect(mockConfig.getInt("defaultDemandClass", 0)).andReturn(0).anyTimes();
        expect(mockConfig.getStringArray("supplyClassFloors")).andReturn(
            "2.916,2.041,1.429,1.0,0.7,0.49,0.343,0.24,0.168".split(",")).anyTimes();
        replay(mockConfig);

        sMDE = createMock(SiteMetaDataEntity.class);
        expect(sMDE.getAdvertisersIncludedBySite()).andReturn(emptySet).anyTimes();
        expect(sMDE.getAdvertisersIncludedByPublisher()).andReturn(emptySet2).anyTimes();
        replay(sMDE);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySiteMetaDetaRepository("siteid")).andReturn(sMDE).anyTimes();
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 1)).andReturn(null).anyTimes();
        replay(repositoryHelper);

        s1 = createMock(ChannelSegmentEntity.class);
        expect(s1.isUdIdRequired()).andReturn(false).anyTimes();
        expect(s1.isZipCodeRequired()).andReturn(false).anyTimes();
        expect(s1.isLatlongRequired()).andReturn(false).anyTimes();
        expect(s1.isRestrictedToRichMediaOnly()).andReturn(false).anyTimes();
        expect(s1.isInterstitialOnly()).andReturn(false).anyTimes();
        expect(s1.isNonInterstitialOnly()).andReturn(false).anyTimes();
        replay(s1);
        s2 = createMock(ChannelSegmentEntity.class);
        expect(s2.isUdIdRequired()).andReturn(true).anyTimes();
        expect(s2.isZipCodeRequired()).andReturn(true).anyTimes();
        expect(s2.isLatlongRequired()).andReturn(true).anyTimes();
        expect(s2.isRestrictedToRichMediaOnly()).andReturn(true).anyTimes();
        expect(s2.isInterstitialOnly()).andReturn(true).anyTimes();
        expect(s2.isNonInterstitialOnly()).andReturn(false).anyTimes();
        expect(s2.getAdvertiserId()).andReturn("advertiserId1").anyTimes();
        replay(s2);
        sasParams = new SASRequestParameters();
        sasParams.setUidParams("xxx");
        sasParams.setPostalCode(110051);
        sasParams.setLatLong("11.35&12.56");
        sasParams.setRichMedia(true);
        sasParams.setRqAdType("int");
        sasParams.setSiteId("siteid");
        Filters.init(mockAdapterConfig);
    }

    @Test
    public void testIsBurnLimitExceeded() {
        Filters filter = new Filters(null, mockConfig, mockAdapterConfig, null, null);
        assertEquals(false, filter.isAdvertiserBurnLimitExceeded(channelSegment1));
        assertEquals(true, filter.isAdvertiserBurnLimitExceeded(channelSegment4));
        assertEquals(false, filter.isAdvertiserBurnLimitExceeded(channelSegment6));
    }

    @Test
    public void testIsDailyImpressionCeilingExceeded() {
        Filters filter = new Filters(null, mockConfig, mockAdapterConfig, null, null);
        assertEquals(true, filter.isAdvertiserDailyImpressionCeilingExceeded(channelSegment1));
        assertEquals(false, filter.isAdvertiserDailyImpressionCeilingExceeded(channelSegment4));
        assertEquals(false, filter.isAdvertiserDailyImpressionCeilingExceeded(channelSegment6));
    }

    @Test
    public void testIsDailyRequestCapExceeded() {
        Filters filter = new Filters(null, mockConfig, mockAdapterConfig, null, null);
        assertEquals(true, filter.isAdvertiserDailyRequestCapExceeded(channelSegment1));
        assertEquals(true, filter.isAdvertiserDailyRequestCapExceeded(channelSegment4));
        assertEquals(false, filter.isAdvertiserDailyRequestCapExceeded(channelSegment6));
    }

    @Test
    public void testAdvertiserLevelFilter() {
        HashMap<String, HashMap<String, ChannelSegment>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegment>>();
        HashMap<String, ChannelSegment> adv1 = new HashMap<String, ChannelSegment>();
        HashMap<String, ChannelSegment> adv2 = new HashMap<String, ChannelSegment>();
        HashMap<String, ChannelSegment> adv3 = new HashMap<String, ChannelSegment>();
        adv1.put(channelSegmentEntity1.getAdgroupId(), channelSegment1);
        adv1.put(channelSegmentEntity2.getAdgroupId(), channelSegment2);
        adv1.put(channelSegmentEntity3.getAdgroupId(), channelSegment3);
        adv2.put(channelSegmentEntity4.getAdgroupId(), channelSegment4);
        adv2.put(channelSegmentEntity5.getAdgroupId(), channelSegment5);
        adv3.put(channelSegmentEntity6.getAdgroupId(), channelSegment6);
        matchedSegments.put(channelSegmentEntity1.getAdvertiserId(), adv1);
        matchedSegments.put(channelSegmentEntity4.getAdvertiserId(), adv2);
        matchedSegments.put(channelSegmentEntity6.getAdvertiserId(), adv3);
        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSiteId("siteid");
        Filters f1 = new Filters(matchedSegments, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        f1.advertiserLevelFiltering();
        assertEquals(1, f1.getMatchedSegments().size());
        assertEquals(false, f1.getMatchedSegments().containsKey(channelSegmentEntity1.getAdvertiserId()));
        assertEquals(false, f1.getMatchedSegments().containsKey(channelSegmentEntity4.getAdvertiserId()));
        assertEquals(true, f1.getMatchedSegments().containsKey(channelSegmentEntity6.getAdvertiserId()));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedWhenNosegmentFlag() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, null, repositoryHelper);
        assertEquals(false, f1.isAnySegmentPropertyViolated(new ChannelSegment(s1, null, null, null, null, null, 0)));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedWhenUdIdFlagSet() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
        sasParams.setUidParams(null);
        assertEquals(true, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedWhenZipCodeFlagSet() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);

        sasParams.setPostalCode(null);
        assertEquals(true, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedLatlongFlagSet() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);

        sasParams.setLatLong(null);
        assertEquals(true, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedRichMediaFlagSet() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
        sasParams.setRichMedia(false);
        assertEquals(true, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
    }

    @Test
    public void testIsAnySegmentPropertyViolatedInterstitialFlagSet() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
        sasParams.setRqAdType(null);
        assertEquals(true, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
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
        expect(s2.getAdvertiserId()).andReturn("advertiserId1").anyTimes();
        replay(s2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
        sasParams.setRqAdType(null);
        assertEquals(false, f1.isAnySegmentPropertyViolated(new ChannelSegment(s2, null, null, null, null, null, 0)));
    }

    @Test
    public void testAdGroupLevelFiltering() {
        HashMap<String, HashMap<String, ChannelSegment>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegment>>();
        HashMap<String, ChannelSegment> adv1 = new HashMap<String, ChannelSegment>();
        HashMap<String, ChannelSegment> adv2 = new HashMap<String, ChannelSegment>();
        HashMap<String, ChannelSegment> adv3 = new HashMap<String, ChannelSegment>();
        adv1.put(channelSegmentEntity1.getAdgroupId(), channelSegment1);
        adv1.put(channelSegmentEntity2.getAdgroupId(), channelSegment2);
        adv1.put(channelSegmentEntity3.getAdgroupId(), channelSegment3);
        adv2.put(channelSegmentEntity4.getAdgroupId(), channelSegment4);
        adv2.put(channelSegmentEntity5.getAdgroupId(), channelSegment5);
        adv3.put(channelSegmentEntity6.getAdgroupId(), channelSegment6);
        matchedSegments.put(channelSegmentEntity1.getAdvertiserId(), adv1);
        matchedSegments.put(channelSegmentEntity4.getAdvertiserId(), adv2);
        matchedSegments.put(channelSegmentEntity6.getAdvertiserId(), adv3);
        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSiteFloor(0.3);
        sasParams.setCountryId(1l);
        sasParams.setOsId(1);
        sasParams.setDst(2);
        sasParams.setSiteId("siteid");
        Filters f1 = new Filters(matchedSegments, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        f1.adGroupLevelFiltering();
        assertEquals(false, f1.getMatchedSegments().get("advertiserId1").containsKey("adgroupId1"));
        assertEquals(2, f1.getMatchedSegments().get("advertiserId1").size());
        assertEquals(1, f1.getMatchedSegments().get("advertiserId2").size());
        assertEquals(1, f1.getMatchedSegments().get("advertiserId3").size());
        assertEquals(false, f1.getMatchedSegments().get("advertiserId1").containsKey("adgroupId1"));
        assertEquals(false, f1.getMatchedSegments().get("advertiserId2").containsKey("adgroupId4"));
    }

    @Test
    public void testSelectTopAdgroupsForRequest() {
        HashMap<String, HashMap<String, ChannelSegment>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegment>>();
        HashMap<String, ChannelSegment> adv1 = new HashMap<String, ChannelSegment>();
        HashMap<String, ChannelSegment> adv2 = new HashMap<String, ChannelSegment>();
        HashMap<String, ChannelSegment> adv3 = new HashMap<String, ChannelSegment>();
        adv1.put(channelSegmentEntity1.getAdgroupId(), channelSegment1);
        adv1.put(channelSegmentEntity2.getAdgroupId(), channelSegment2);
        adv1.put(channelSegmentEntity3.getAdgroupId(), channelSegment3);
        adv2.put(channelSegmentEntity4.getAdgroupId(), channelSegment4);
        adv2.put(channelSegmentEntity5.getAdgroupId(), channelSegment5);
        adv3.put(channelSegmentEntity6.getAdgroupId(), channelSegment6);
        matchedSegments.put(channelSegmentEntity1.getAdvertiserId(), adv1);
        matchedSegments.put(channelSegmentEntity4.getAdvertiserId(), adv2);
        matchedSegments.put(channelSegmentEntity6.getAdvertiserId(), adv3);
        Filters f1 = new Filters(matchedSegments, mockConfig, mockAdapterConfig, null, null);
        List<ChannelSegment> finalRow = f1.convertToSegmentsList(matchedSegments);
        assertEquals(6, finalRow.size());
        finalRow = f1.selectTopAdGroupsForRequest(finalRow);
        assertEquals(5, finalRow.size());
        assertEquals("adgroupId2", finalRow.get(0).getChannelSegmentEntity().getAdgroupId());
        assertEquals("adgroupId1", finalRow.get(4).getChannelSegmentEntity().getAdgroupId());
    }

    @Test
    public void testIsAdvertiserExcludedWhenSiteInclusionListEmptyPublisherInclusionListEmpty() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isAdvertiserExcluded(channelSegment1));
    }

    @Test
    public void testIsAdvertiserExcludedWhenSiteInclusionListEmpty() {
        emptySet2.add("123");
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isAdvertiserExcluded(channelSegment1));
        emptySet2.add("advertiserId1");
        assertEquals(false, f1.isAdvertiserExcluded(channelSegment1));
        emptySet2.clear();
        assertEquals(false, f1.isAdvertiserExcluded(channelSegment1));
    }

    @Test
    public void testIsAdvertiserExcludedWhenPublisherInclusionListEmpty() {
        emptySet.add("123");
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isAdvertiserExcluded(channelSegment1));
        emptySet.add("advertiserId1");
        assertEquals(false, f1.isAdvertiserExcluded(channelSegment1));
        emptySet.clear();
        assertEquals(false, f1.isAdvertiserExcluded(channelSegment1));
    }

    @Test
    public void testIsAdvertiserExcludedWhenSiteInclusionListNotEmptyPublisherInclusionListNotEmpty() {
        emptySet.add("123");
        emptySet2.add("advertiserId1");
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isAdvertiserExcluded(channelSegment1));
        emptySet.add("advertiserId1");
        assertEquals(false, f1.isAdvertiserExcluded(channelSegment1));
        emptySet.clear();
        emptySet2.remove("advertiserId1");
        emptySet2.add("123");
        assertEquals(true, f1.isAdvertiserExcluded(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserInclusionTrueEmptyList() {
        ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId("advertiserId1");
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(true);
        cE1Builder.setSitesIE(emptySet);
        cE1 = cE1Builder.build();
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null, null, cSFE1.getECPM());
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isSiteExcludedByAdvertiser(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserInclusionTrueNonEmptyList() {
        ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId("advertiserId1");
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(true);
        cE1Builder.setSitesIE(emptySet);
        cE1 = cE1Builder.build();
        emptySet.add("siteid1");
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null, null, cSFE1.getECPM());
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isSiteExcludedByAdvertiser(channelSegment1));
        emptySet.add("siteid");
        assertEquals(false, f1.isSiteExcludedByAdvertiser(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserExclusionTrueEmptyList() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isSiteExcludedByAdvertiser(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdvertiserExclusionTrueNonEmptyList() {
        emptySet.add("siteid1");
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isSiteExcludedByAdvertiser(channelSegment1));
        emptySet.add("siteid");
        assertEquals(true, f1.isSiteExcludedByAdvertiser(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdGroupInclusionTrueEmptyList() {
        System.out.print(channelSegment1.getChannelSegmentEntity().isSiteInclusion());
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isSiteExcludedByAdGroup(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdGroupInclusionTrueNonEmptyList() {
        ChannelEntity.Builder cE1Builder = ChannelEntity.newBuilder();
        cE1Builder.setChannelId("advertiserId1");
        cE1Builder.setPriority(1);
        cE1Builder.setImpressionCeil(90);
        cE1Builder.setName("name1");
        cE1Builder.setRequestCap(100);
        cE1Builder.setSiteInclusion(true);
        cE1Builder.setSitesIE(emptySet);
        channelSegment1 = new ChannelSegment(channelSegmentEntity1, cE1, cFE1, cSFE1, null, null, cSFE1.getECPM());
        cE1 = cE1Builder.build();
        emptySet.add("siteid1");
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isSiteExcludedByAdGroup(channelSegment1));
        emptySet.add("siteid");
        assertEquals(false, f1.isSiteExcludedByAdGroup(channelSegment1));
    }

    @Test
    public void testIsSiteExcludedByAdGroupExclusionTrueEmptyList() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isSiteExcludedByAdGroup(channelSegment2));
    }

    @Test
    public void testIsSiteExcludedByAdGroupExclusionTrueNonEmptyList() {
        emptySet.add("siteid1");
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(false, f1.isSiteExcludedByAdGroup(channelSegment2));
        emptySet.add("siteid");
        assertEquals(true, f1.isSiteExcludedByAdGroup(channelSegment2));
    }

    @Test
    public void testIsAdGroupDailyImpressionCeilingExceeded() {
        Filters filter = new Filters(null, mockConfig, mockAdapterConfig, null, null);
        assertEquals(true, filter.isAdGroupDailyImpressionCeilingExceeded(channelSegment1));
        assertEquals(false, filter.isAdGroupDailyImpressionCeilingExceeded(channelSegment4));
        assertEquals(false, filter.isAdGroupDailyImpressionCeilingExceeded(channelSegment6));
    }

    @Test
    public void testGetEpmClass() {
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(0, f1.getEcpmClass(3.0, 1.0));
        assertEquals(1, f1.getEcpmClass(2.1, 1.0));
        assertEquals(2, f1.getEcpmClass(1.5, 1.0));
        assertEquals(3, f1.getEcpmClass(1.1, 1.0));
        assertEquals(4, f1.getEcpmClass(0.8, 1.0));
        assertEquals(5, f1.getEcpmClass(0.6, 1.0));
        assertEquals(6, f1.getEcpmClass(0.4, 1.0));
        assertEquals(7, f1.getEcpmClass(0.3, 1.0));
        assertEquals(8, f1.getEcpmClass(0.2, 1.0));
        assertEquals(9, f1.getEcpmClass(0.1, 1.0));
    }

    @Test
    public void testGetSupplyClass() {
        SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.setSiteId("siteid");
        builder.setCountryId(1);
        builder.setOsId(1);
        builder.setEcpm(3.0);
        builder.setNetworkEcpm(1.0);
        SiteEcpmEntity siteEcpmEntity = builder.build();
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySiteEcpmRepository("siteid", 1, 2)).andReturn(siteEcpmEntity).anyTimes();
        replay(repositoryHelper);
        sasParams.setSiteId("siteid");
        sasParams.setCountryId(1l);
        sasParams.setOsId(2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(0, f1.getSupplyClass(sasParams));
    }

    @Test
    public void testIsDemandAcceptedBySupplyWithDefaultSupplyClassDefaultDemandClass() {
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(null).anyTimes();
        sasParams.setCountryId(1l);
        sasParams.setOsId(2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        assertEquals(true, f1.isDemandAcceptedBySupply(channelSegment1));
    }

    @Test
    public void testIsDemandAcceptedBySupplyWithDefaultDemandClass() {
        SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.setSiteId("siteid");
        builder.setCountryId(1);
        builder.setOsId(1);
        builder.setEcpm(3.0);
        builder.setNetworkEcpm(1.0);
        SiteEcpmEntity siteEcpmEntity = builder.build();
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(null).anyTimes();
        replay(repositoryHelper);
        sasParams.setSiteId("siteid");
        sasParams.setCountryId(1l);
        sasParams.setOsId(2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        f1.setSiteEcpmEntity(siteEcpmEntity);
        channelSegment1.setPrioritisedECPM(3.0);
        assertEquals(true, f1.isDemandAcceptedBySupply(channelSegment1));
    }

    @Test
    public void testIsDemandAcceptedBySupplyWithDefaultSupplyClass() {
        repositoryHelper = createMock(RepositoryHelper.class);
        PricingEngineEntity.Builder builder = PricingEngineEntity.newBuilder();
        builder.setCountryId(1);
        builder.setOsId(2);
        builder.setSupplyToDemandMap(new HashedMap(ImmutableMap.of("1", ImmutableSet.of("2"))));
        PricingEngineEntity pricingEngineEntity = builder.build();
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(pricingEngineEntity).anyTimes();
        replay(repositoryHelper);
        sasParams.setSiteId("siteid");
        sasParams.setCountryId(1l);
        sasParams.setOsId(2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        channelSegment1.setPrioritisedECPM(3.0);
        f1.fetchPricingEngineEntity();
        assertEquals(true, f1.isDemandAcceptedBySupply(channelSegment1));
    }

    @Test
    public void testIsDemandAcceptedBySupplyPass() {
        SiteEcpmEntity.Builder builder1 = SiteEcpmEntity.newBuilder();
        builder1.setSiteId("siteid");
        builder1.setCountryId(1);
        builder1.setOsId(1);
        builder1.setEcpm(3.0);
        builder1.setNetworkEcpm(1.0);
        SiteEcpmEntity siteEcpmEntity = builder1.build();
        repositoryHelper = createMock(RepositoryHelper.class);
        PricingEngineEntity.Builder builder2 = PricingEngineEntity.newBuilder();
        builder2.setCountryId(1);
        builder2.setOsId(2);
        builder2.setSupplyToDemandMap(new HashedMap(ImmutableMap.of("0", ImmutableSet.of("0", "1"))));
        PricingEngineEntity pricingEngineEntity = builder2.build();
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(pricingEngineEntity).anyTimes();
        replay(repositoryHelper);
        sasParams.setSiteId("siteid");
        sasParams.setCountryId(1l);
        sasParams.setOsId(2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        f1.fetchPricingEngineEntity();
        f1.setSiteEcpmEntity(siteEcpmEntity);
        channelSegment1.setPrioritisedECPM(3.0);
        assertEquals(true, f1.isDemandAcceptedBySupply(channelSegment1));
    }

    @Test
    public void testIsDemandAcceptedBySupplyFail() {
        SiteEcpmEntity.Builder builder1 = SiteEcpmEntity.newBuilder();
        builder1.setSiteId("siteid");
        builder1.setCountryId(1);
        builder1.setOsId(1);
        builder1.setEcpm(3.0);
        builder1.setNetworkEcpm(1.0);
        SiteEcpmEntity siteEcpmEntity = builder1.build();
        PricingEngineEntity.Builder builder2 = PricingEngineEntity.newBuilder();
        builder2.setCountryId(1);
        builder2.setOsId(2);
        builder2.setSupplyToDemandMap(new HashedMap(ImmutableMap.of("0", ImmutableSet.of("1"))));
        PricingEngineEntity pricingEngineEntity = builder2.build();
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryPricingEngineRepository(1, 2)).andReturn(pricingEngineEntity).anyTimes();
        replay(repositoryHelper);
        sasParams.setSiteId("siteid");
        sasParams.setCountryId(1l);
        sasParams.setOsId(2);
        Filters f1 = new Filters(null, mockConfig, mockAdapterConfig, sasParams, repositoryHelper);
        f1.fetchPricingEngineEntity();
        f1.setSiteEcpmEntity(siteEcpmEntity);
        channelSegment1.setPrioritisedECPM(3.0);
        assertEquals(false, f1.isDemandAcceptedBySupply(channelSegment1));
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
            final JSONObject additionalParams, final List<Integer> manufModelTargetingList, final double ecpmBoost,
            final Timestamp eCPMBoostDate, final Long[] tod, final boolean siteInclusion, final Set<String> siteIE,
            final int impressionCeil) {
        ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setAdvertiserId(advertiserId);
        builder.setAdgroupId(adgroupId);
        builder.setAdId(adId);
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
        builder.setIncId(incId);
        builder.setAdgroupIncId(incId);
        builder.setPricingModel(pricingModel);
        builder.setSiteRatings(siteRatings);
        builder.setTargetingPlatform(targetingPlatform);
        builder.setOsIds(osIds);
        builder.setUdIdRequired(udIdRequired);
        builder.setLatlongRequired(latlongRequired);
        builder.setStripZipCode(zipCodeRequired);
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
            final int todayImpressions, final int todayRequests, final double averageLatency, final double revenue) {
        ChannelFeedbackEntity.Builder builder = ChannelFeedbackEntity.newBuilder();
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
            final String adGroupId, final double eCPM, final double fillRatio, final double latency,
            final int requests, final int beacons, final int clicks, final int todaysImpressions) {
        ChannelSegmentFeedbackEntity.Builder builder = ChannelSegmentFeedbackEntity.newBuilder();
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
