package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.api.SASRequestParameters.NappScore.CONFIDENT_GOOD_SCORE;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.circuitbreaker.CircuitBreakerImpl;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplierTest;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserFailureThrottler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.Ad;
import com.inmobi.casthrift.AdIdChain;
import com.inmobi.casthrift.AdMeta;
import com.inmobi.casthrift.AdRR;
import com.inmobi.casthrift.AdStatus;
import com.inmobi.casthrift.AuctionInfo;
import com.inmobi.casthrift.CasAdChain;
import com.inmobi.casthrift.Channel;
import com.inmobi.casthrift.ContentRating;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.casthrift.Gender;
import com.inmobi.casthrift.Geo;
import com.inmobi.casthrift.HandsetMeta;
import com.inmobi.casthrift.Impression;
import com.inmobi.casthrift.InventoryType;
import com.inmobi.casthrift.IxAd;
import com.inmobi.casthrift.PricingModel;
import com.inmobi.casthrift.RTBDAuctionInfo;
import com.inmobi.casthrift.Request;
import com.inmobi.casthrift.User;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Logging.class, InspectorStats.class, CircuitBreakerImpl.class, AdvertiserFailureThrottler.class})
public class LoggingTest {
    private static final String DEFAULT_HOST = "http://default.host";
    private static Configuration mockConfig;
    private static final int sampledadvertisercount = 5;
    private final Set<String> emptySet = new HashSet<>();

    @BeforeClass
    public static void setUp() {
        mockConfig = createMock(Configuration.class);
        final AbstractMessagePublisher mockDataBusPublisher = createNiceMock(AbstractMessagePublisher.class);

        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getString("advertiser")).andReturn("advertiser").anyTimes();
        expect(mockConfig.getString("sampledadvertiser")).andReturn("sampledadvertiser").anyTimes();
        expect(mockConfig.getBoolean("enableFileLogging")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean("enableDatabusLogging")).andReturn(true).anyTimes();
        expect(mockConfig.getInt("sampledadvertisercount")).andReturn(sampledadvertisercount).anyTimes();
        replayAll();

        Logging.init(mockDataBusPublisher, "null", "null", "null", mockConfig, "hostName", "corp");
    }

    /**
     * Branches/Conditions followed:
     * SasParams is null
     * TerminationReason is not null => isTerminated is true
     * ChannelSegment is null
     * => impression object is null
     * => adserved is 0
     * => slotServed is null
     * => requestSlot is also null
     * rankList is null
     * => ChannelsLog is empty
     */
    @Test
    public void testRequestResponseLoggingVariation1() throws Exception {
        final String terminationReason = "TERMINATION_REASON";
        final String host = "hostName";

        mockStaticNice(InspectorStats.class);
        mockStaticNice(InetAddress.class);
        final InetAddress mockInetAddress = createMock(InetAddress.class);

        expect(InetAddress.getLocalHost()).andReturn(mockInetAddress).times(1);
        expect(mockInetAddress.getHostName()).andReturn(host).times(1);

        replayAll();

        final AdRR adRR = Logging.getAdRR(null, null, null, null, terminationReason);
        final Request request = adRR.getRequest();
        final User user = request.getUser();
        final HandsetMeta handsetMeta = request.getHandset();

        assertThat(adRR.getTermination_reason(), is(equalTo(terminationReason)));
        assertThat(adRR.isIs_terminated(), is(equalTo(true)));
        assertThat(adRR.getImpressions(), is(equalTo(null)));
        assertThat(request.getSite(), is(equalTo(null)));
        assertThat(request.getId(), is(equalTo(null)));
        assertThat(request.getN_ads_requested(), is(equalTo((short) 1)));
        assertThat(request.getN_ads_served(), is(equalTo((short) 0)));
        assertThat(request.getIP(), is(equalTo(null)));
        assertThat(user.isSetAge(), is(equalTo(false)));
        assertThat(user.isSetGender(), is(equalTo(false)));
        assertThat(handsetMeta.isSetId(), is(equalTo(false)));
        assertThat(handsetMeta.isSetOsId(), is(equalTo(false)));
        assertThat(request.getInventory(), is(equalTo(InventoryType.APP)));
    }

    /**
     * Branches/Conditions followed:
     * SasParams is not null
     * TerminationReason is null => isTerminated is false
     * ChannelSegment is not null
     * requestSlot is not null
     * rankList is null
     */
    @Test
    public void testRequestResponseLoggingVariation2() throws Exception {
        String terminationReason = null;
        String host = "hostName";
        String siteId = "siteId";
        String taskId = "taskId";
        String adId = "adId";
        Long adIncId = 1234L;
        String adgroupId = "adgroupId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String impressionId = "impressionId";
        String gender = "m";

        int osId = 3;
        int dst = DemandSourceType.RTBD.getValue();
        long adgroupIncId = 456L;
        double demandDensity = 15.0;
        double longTermRevenue = 30.0;
        int publisherYield = 13;

        Short age = 11;
        Short selectedSlot = 11;
        Integer carrierId = 345;
        Integer state = 123;
        Integer city = 567;
        Integer siteSegmentIds = 45;
        Long handsetInternalId = 123L;
        Long campaignIncId = 123L;
        Long countryId = 94L;
        Double secondBidPriceInUsd = 4.5;
        Double marketRate = 567.98;
        ADCreativeType adCreativeType = ADCreativeType.BANNER;
        final String appBundleId = "testAppBundleId";
        final List<Short> slotList = new ArrayList<Short>();
        slotList.add((short) 2);
        slotList.add((short) 4);

        mockStaticNice(InspectorStats.class);
        mockStaticNice(InetAddress.class);
        InetAddress mockInetAddress = createMock(InetAddress.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        CasInternalRequestParameters mockCasInternalRequestParameters = createMock(CasInternalRequestParameters.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        BaseAdNetworkImpl mockAdNetworkInterface = createMock(BaseAdNetworkImpl.class);

        expect(InetAddress.getLocalHost()).andReturn(mockInetAddress).times(1);
        expect(mockInetAddress.getHostName()).andReturn(host).times(1);
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockAdNetworkInterface.getCreativeType()).andReturn(adCreativeType).anyTimes();
        expect(mockAdNetworkInterface.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUsd).anyTimes();
        expect(mockAdNetworkInterface.getCreativeId()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("name").anyTimes();
        expect(mockAdNetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdNetworkInterface.getSelectedSlotId()).andReturn(selectedSlot).anyTimes();
        expect(mockAdNetworkInterface.getDeal()).andReturn(null).anyTimes();
        expect(mockChannelSegmentEntity.getAdId(adCreativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adgroupId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(dst).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getSiteContentType()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getSiteId()).andReturn(siteId).anyTimes();
        expect(mockSASRequestParameters.getTid()).andReturn(taskId).anyTimes();
        expect(mockSASRequestParameters.getCountryId()).andReturn(countryId).anyTimes();
        expect(mockSASRequestParameters.getCarrierId()).andReturn(carrierId).anyTimes();
        expect(mockSASRequestParameters.getState()).andReturn(state).anyTimes();
        expect(mockSASRequestParameters.getCity()).andReturn(city).anyTimes();
        expect(mockSASRequestParameters.getDst()).andReturn(dst).anyTimes();
        expect(mockSASRequestParameters.getAge()).andReturn(age).anyTimes();
        expect(mockSASRequestParameters.getGender()).andReturn(gender).anyTimes();
        expect(mockSASRequestParameters.getTUidParams()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getMarketRate()).andReturn(marketRate).anyTimes();
        expect(mockSASRequestParameters.getHandsetInternalId()).andReturn(handsetInternalId).anyTimes();
        expect(mockSASRequestParameters.getOsId()).andReturn(osId).anyTimes();
        expect(mockSASRequestParameters.getSdkVersion()).andReturn("0").anyTimes();
        expect(mockSASRequestParameters.getSiteSegmentId()).andReturn(siteSegmentIds).anyTimes();
        expect(mockSASRequestParameters.getAppBundleId()).andReturn(appBundleId).anyTimes();
        expect(mockSASRequestParameters.getPlacementId()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getIntegrationDetails()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getNormalizedUserId()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getRqMkSlot()).andReturn(slotList).anyTimes();
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();
        expect(mockCasInternalRequestParameters.getDemandDensity()).andReturn(demandDensity).anyTimes();
        expect(mockCasInternalRequestParameters.getLongTermRevenue()).andReturn(longTermRevenue).anyTimes();
        expect(mockCasInternalRequestParameters.getPublisherYield()).andReturn(publisherYield).anyTimes();
        expect(mockCasInternalRequestParameters.getImeiMD5()).andReturn("dummy").anyTimes();
        expect(mockCasInternalRequestParameters.getImeiSHA1()).andReturn("dummy").anyTimes();
        replayAll();

        AdRR adRR =
                Logging.getAdRR(mockChannelSegment, null, mockSASRequestParameters, mockCasInternalRequestParameters,
                        terminationReason);
        Request request = adRR.getRequest();
        AuctionInfo auctionInfo = adRR.getAuction_info();
        User user = request.getUser();
        HandsetMeta handsetMeta = request.getHandset();
        Impression impression = adRR.getImpressions().get(0);
        Ad ad = impression.getAd();
        AdMeta adMeta = ad.getMeta();
        AdIdChain adIdChain = ad.getId();
        Geo geo = request.getIP();
        RTBDAuctionInfo rtbdAuctionInfo = auctionInfo.getRtbd_auction_info();

        assertThat(adRR.getTermination_reason(), is(equalTo("NO")));
        assertThat(adRR.isIs_terminated(), is(equalTo(false)));
        assertThat(impression.getImpressionId(), is(equalTo(impressionId)));
        assertThat(ad.getWinBid(), is(equalTo(secondBidPriceInUsd)));
        assertThat(adMeta.getCr(), is(equalTo(null)));
        assertThat(adMeta.getPricing(), is(equalTo(null)));

        assertThat(adIdChain.getAd(), is(equalTo(adId)));
        assertThat(adIdChain.getAdvid(), is(equalTo(advertiserId)));
        assertThat(adIdChain.getCampaign(), is(equalTo(campaignId)));
        assertThat(adIdChain.getExternal_site(), is(equalTo(externalSiteKey)));
        assertThat(adIdChain.getGroup(), is(equalTo(adgroupId)));

        assertThat(rtbdAuctionInfo.getDemand_density(), is(equalTo(demandDensity / marketRate)));
        assertThat(rtbdAuctionInfo.getInmobi_ltr(), is(equalTo(longTermRevenue / marketRate)));
        assertThat(rtbdAuctionInfo.getPublisher_yield(), is(equalTo(publisherYield)));

        assertThat(request.getSite(), is(equalTo(siteId)));
        assertThat(request.getId(), is(equalTo(taskId)));
        assertThat(request.getN_ads_requested(), is(equalTo((short) 1)));
        assertThat(request.getN_ads_served(), is(equalTo((short) 1)));
        assertThat(request.getSlot_served(), is(equalTo(selectedSlot)));
        assertThat(request.getSlot_requested(), is(equalTo(selectedSlot)));
        assertThat(request.getSegmentId(), is(siteSegmentIds));
        assertThat(request.getRequestDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(request.isSetAuctionBidFloor(), is(false));
        assertThat(request.isSetBidGuidance(), is(false));
        assertThat(request.isSetImeiPresent(), is(true));

        assertThat(geo.getCarrier(), is(carrierId));
        assertThat(geo.getCountry(), is(countryId.shortValue()));
        assertThat(geo.getCity(), is(city));
        assertThat(geo.getRegion(), is(state));

        assertThat(user.getAge(), is(equalTo(age)));
        assertThat(user.getGender(), is(equalTo(Gender.MALE)));

        assertThat(handsetMeta.getId(), is(equalTo(handsetInternalId.intValue())));
        assertThat(handsetMeta.getOsId(), is(equalTo(osId)));

        assertThat(request.getInventory(), is(equalTo(InventoryType.BROWSER)));
    }

    @Test
    public void testCreativeLoggingNoLoggingDone() throws Exception {
        Logging.creativeLogging(null, null);
        Logging.creativeLogging(new ArrayList<>(), null);

        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);

        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockAdNetworkInterface.isRtbPartner()).andReturn(true).times(1).andReturn(false).times(1)
                .andReturn(false).times(1);

        expect(mockAdNetworkInterface.isIxPartner()).andReturn(false).anyTimes();
        expect(mockAdNetworkInterface.isLogCreative()).andReturn(false).times(1).andReturn(true).times(1)
                .andReturn(false).times(1);

        replayAll();

        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
    }

    @Test
    public void testCreativeLogging() throws Exception {
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String httpResponseContent = "httpResponseContent";
        String adMarkup = "adMarkup";
        String requestUrl = "requestUrl";
        String name = "RTBD Partner";
        String adStatus = "AD";
        String iUrl = "imageURL";
        String creativeId = "creativeId";
        List<Integer> attributes = Arrays.asList(4, 6);
        List<String> aDomain = Arrays.asList("Domain1", "Domain2");
        Long countryId = 94L;

        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);

        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockAdNetworkInterface.isRtbPartner()).andReturn(true).anyTimes();
        expect(mockAdNetworkInterface.isLogCreative()).andReturn(true).anyTimes();
        expect(mockAdNetworkInterface.getHttpResponseContent()).andReturn(httpResponseContent).anyTimes();
        expect(mockAdNetworkInterface.getCreativeType()).andReturn(ADCreativeType.NATIVE).times(2)
                .andReturn(ADCreativeType.BANNER).times(2);
        expect(mockAdNetworkInterface.getAdMarkUp()).andReturn(adMarkup).anyTimes();
        expect(mockAdNetworkInterface.getRequestUrl()).andReturn(requestUrl).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn(name).anyTimes();
        expect(mockAdNetworkInterface.getId()).andReturn("ID").anyTimes();
        expect(mockAdNetworkInterface.getIUrl()).andReturn(iUrl).anyTimes();
        expect(mockAdNetworkInterface.getAttribute()).andReturn(attributes).anyTimes();
        expect(mockAdNetworkInterface.getADomain()).andReturn(aDomain).anyTimes();
        expect(mockAdNetworkInterface.getCreativeId()).andReturn(creativeId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockThirdPartyAdResponse.getAdStatus()).andReturn(adStatus).anyTimes();
        expect(mockSASRequestParameters.getCountryId()).andReturn(countryId).anyTimes();
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();



        replayAll();

        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
    }

    @Test
    public void testIXCreativeLogging() throws Exception {
        String advertiserId = "advertiserId";
        String externalSiteKey = null;
        String httpResponseContent = "httpResponseContent";
        String adMarkup = "adMarkup";
        String requestUrl = "requestUrl";
        String name = "IX Partner";
        String adStatus = "AD";
        String iUrl = null;
        String creativeId = "rtb:1234:5678";
        List<Integer> attributes = Arrays.asList(4, 6);
        List<String> aDomain = Arrays.asList("Domain1", "Domain2");
        Long countryId = 94L;

        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);

        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockAdNetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        expect(mockAdNetworkInterface.isIxPartner()).andReturn(true).times(3);
        expect(mockAdNetworkInterface.isLogCreative()).andReturn(true).times(1).andReturn(false).anyTimes();
        expect(mockAdNetworkInterface.getHttpResponseContent()).andReturn(httpResponseContent).anyTimes();
        expect(mockAdNetworkInterface.getCreativeType()).andReturn(ADCreativeType.INTERSTITIAL_VIDEO).times(2)
                .andReturn(ADCreativeType.NATIVE).times(2).andReturn(ADCreativeType.BANNER).times(2);
        expect(mockAdNetworkInterface.getAdMarkUp()).andReturn(adMarkup).times(1);
        expect(mockAdNetworkInterface.getRequestUrl()).andReturn(requestUrl).times(1);
        expect(mockAdNetworkInterface.getName()).andReturn(name).times(1);
        expect(mockAdNetworkInterface.getId()).andReturn("ID").times(1);
        expect(mockAdNetworkInterface.getIUrl()).andReturn(iUrl).times(1);
        expect(mockAdNetworkInterface.getAttribute()).andReturn(attributes).times(1);
        expect(mockAdNetworkInterface.getADomain()).andReturn(aDomain).times(1);
        expect(mockAdNetworkInterface.getCreativeId()).andReturn(creativeId).times(1);

        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).times(1);
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).times(1);
        expect(mockThirdPartyAdResponse.getAdStatus()).andReturn(adStatus).times(1);
        expect(mockSASRequestParameters.getCountryId()).andReturn(countryId).times(1);
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();
        replayAll();

        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
        Logging.creativeLogging(Arrays.asList(mockChannelSegment), mockSASRequestParameters);
    }

    @Test
    public void testCreateChannelsLog() throws Exception {
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String creativeId = "creativeId";
        String[] adStatus = new String[] {"AD", "NO_AD", "TIME_OUT", "SOMETHING_ELSE"};
        int dst = DemandSourceType.IX.getValue();
        long campaignIncId = 123L;
        long adgroupIncId = 456L;
        long adIncId = 678L;
        long latency = 789L;
        long startTime = 723L;
        double bidPriceInUSD = 4.0;
        ADCreativeType adCreativeType = ADCreativeType.BANNER;

        mockStaticNice(InspectorStats.class);
        PowerMock.suppress(AdvertiserFailureThrottler.class
                .getDeclaredMethod("incrementFailureCounter", String.class, long.class));

        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        expect(mockSASRequestParameters.isSandBoxRequest()).andReturn(false).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(dst).anyTimes();
        expect(mockAdNetworkInterface.getLatency()).andReturn(latency).anyTimes();
        expect(mockAdNetworkInterface.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("Name").anyTimes();
        expect(mockAdNetworkInterface.getId()).andReturn("ID").anyTimes();
        expect(mockAdNetworkInterface.getDeal()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getForwardedPackageIds()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getForwardedDealIds()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getShortlistedTargetingSegmentIds()).andReturn(null).anyTimes();
        expect(mockThirdPartyAdResponse.getLatency()).andReturn(latency).anyTimes();
        expect(mockThirdPartyAdResponse.getStartTime()).andReturn(startTime).anyTimes();
        expect(mockAdNetworkInterface.getAdStatus())
                .andReturn(adStatus[0]).times(1)
                .andReturn(adStatus[1]).times(1)
                .andReturn(adStatus[2]).times(1)
                .andReturn(adStatus[3]).times(1);
        expect(mockThirdPartyAdResponse.getAdStatus())
                .andReturn(adStatus[0]).times(2)
                .andReturn(adStatus[1]).times(2)
                .andReturn(adStatus[2]).times(2)
                .andReturn(adStatus[3]).times(2);
        expect(mockAdNetworkInterface.getCreativeId())
                .andReturn(creativeId).times(1)
                .andReturn(null).anyTimes();

        expect(mockAdNetworkInterface.getCreativeType())
                .andReturn(adCreativeType).anyTimes();
        expect(mockAdNetworkInterface.getHostName()).andReturn(DEFAULT_HOST).anyTimes();


        replayAll();

        assertThat(Logging.createChannelsLog(null, mockSASRequestParameters).size(), is(0));

        Channel channel = Logging.createChannelsLog(Arrays.asList(mockChannelSegment), mockSASRequestParameters).get(0);
        assertThat(channel.getAdStatus(), is(equalTo(Logging.getAdStatus(adStatus[0]))));
        assertThat(channel.getLatency(), is(equalTo(latency)));
        CasAdChain casAdChain = channel.getAdChain();
        assertThat(casAdChain.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(casAdChain.getCampaign_inc_id(), is(equalTo(campaignIncId)));
        assertThat(casAdChain.getAdgroup_inc_id(), is(equalTo(adgroupIncId)));
        assertThat(casAdChain.getExternalSiteKey(), is(equalTo(externalSiteKey)));
        assertThat(casAdChain.getDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(casAdChain.getCreativeId(), is(equalTo(creativeId)));

        channel = Logging.createChannelsLog(Arrays.asList(mockChannelSegment), mockSASRequestParameters).get(0);
        casAdChain = channel.getAdChain();
        assertThat(channel.getAdStatus(), is(equalTo(Logging.getAdStatus(adStatus[1]))));
        assertThat(casAdChain.getCreativeId(), is(equalTo(null)));

        channel = Logging.createChannelsLog(Arrays.asList(mockChannelSegment), mockSASRequestParameters).get(0);
        assertThat(channel.getAdStatus(), is(equalTo(Logging.getAdStatus(adStatus[2]))));

        channel = Logging.createChannelsLog(Arrays.asList(mockChannelSegment), mockSASRequestParameters).get(0);
        assertThat(channel.getAdStatus(), is(equalTo(Logging.getAdStatus(adStatus[3]))));
    }

    @Test
    public void testCreateIXChannelsLog() throws Exception {
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String creativeId = "creativeId";
        String dspId = "TEST_DSP_ID";
        String advId = "TEST_ADV_ID";
        String seatId = "TEST_SEAT_ID";
        String aqId = "TEST_AQ_ID";
        Set<Integer> packageIds = ImmutableSet.of(1,2,3);
        int winningPackageId = 2;
        String dealId = "TEST_DEAL_ID";
        String adStatus = "AD";
        int dst = DemandSourceType.IX.getValue();
        long campaignIncId = 123L;
        long adgroupIncId = 456L;
        long adIncId = 678L;
        long latency = 789L;
        double bidPriceInUSD = 4.0;
        ADCreativeType adCreativeType = ADCreativeType.BANNER;

        mockStaticNice(InspectorStats.class);
        IXAdNetwork mockIXAdNetwork = createMock(IXAdNetwork.class);
        AdNetworkInterface mockAdNetworkInterface = mockIXAdNetwork;
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        ChannelSegmentEntity mockChannelSegmentEntityForIncId = createMock(ChannelSegmentEntity.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        expect(mockSASRequestParameters.isSandBoxRequest()).andReturn(false).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(dst).anyTimes();
        expect(mockAdNetworkInterface.getLatency()).andReturn(latency).anyTimes();
        expect(mockAdNetworkInterface.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("Name").anyTimes();
        expect(mockAdNetworkInterface.getForwardedPackageIds()).andReturn(packageIds).anyTimes();
        expect(mockAdNetworkInterface.getDeal()).andReturn(DealEntity.newBuilder().id(dealId).packageId(winningPackageId).build()).anyTimes();
        expect(mockAdNetworkInterface.getShortlistedTargetingSegmentIds()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getForwardedDealIds()).andReturn(null).anyTimes();
        expect(mockIXAdNetwork.getOriginalBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockIXAdNetwork.getDspId()).andReturn(dspId).times(2);
        expect(mockIXAdNetwork.getAdvId()).andReturn(advId).times(2);
        expect(mockIXAdNetwork.getSeatId()).andReturn(seatId).anyTimes();
        expect(mockIXAdNetwork.getAqid()).andReturn(aqId).times(2);
        expect(mockIXAdNetwork.getAdjustbid()).andReturn(0.8).times(2);

        expect(mockThirdPartyAdResponse.getLatency()).andReturn(latency).anyTimes();
        expect(mockAdNetworkInterface.getAdStatus()).andReturn(adStatus).times(1);
        expect(mockThirdPartyAdResponse.getAdStatus()).andReturn(adStatus).times(2);
        expect(mockAdNetworkInterface.getCreativeId()).andReturn(creativeId).times(1).andReturn(null).anyTimes();

        expect(mockAdNetworkInterface.getCreativeType()).andReturn(adCreativeType).anyTimes();

        expect(mockIXAdNetwork.getEntity()).andReturn(mockChannelSegmentEntityForIncId).anyTimes();
        expect(mockIXAdNetwork.getHostName()).andReturn(DEFAULT_HOST).anyTimes();
        expect(mockChannelSegmentEntityForIncId.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntityForIncId.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();


        replayAll();

        assertThat(Logging.createChannelsLog(null, mockSASRequestParameters).size(), is(0));

        Channel channel = Logging.createChannelsLog(Arrays.asList(mockChannelSegment), mockSASRequestParameters).get(0);
        assertThat(channel.getAdStatus(), is(equalTo(Logging.getAdStatus(adStatus))));
        assertThat(channel.getLatency(), is(equalTo(latency)));
        CasAdChain casAdChain = channel.getAdChain();
        assertThat(casAdChain.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(casAdChain.getCampaign_inc_id(), is(equalTo(campaignIncId)));
        assertThat(casAdChain.getAdgroup_inc_id(), is(equalTo(adgroupIncId)));
        assertThat(casAdChain.getExternalSiteKey(), is(equalTo(externalSiteKey)));
        assertThat(casAdChain.getDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(casAdChain.getCreativeId(), is(equalTo(creativeId)));

        IxAd ixAd = channel.getDeprecatedIxAds().get(0);
        assertThat(ixAd.getDspId(), is(equalTo(dspId)));
        assertThat(ixAd.getAdvId(), is(equalTo(advId)));
        assertThat(ixAd.getSeatId(), is(equalTo(seatId)));
        assertThat(ixAd.getAqId(), is(equalTo(aqId)));
        assertTrue(CollectionUtils.isEqualCollection(ixAd.getDeprecatedPackageIds(),packageIds));
        assertThat(ixAd.getDeprecatedWinningPackageId(), is(equalTo(winningPackageId)));
        assertThat(ixAd.getDeprecatedWinningDealId(), is(equalTo(dealId)));
        assertThat(ixAd.getRpAdgroupIncId(), is(equalTo(adgroupIncId)));
        assertThat(ixAd.getRpAdIncId(), is(equalTo(adIncId)));
    }

    @Test
    public void testCreateIXChannelsLogCheckGrossBidForAgencyRebateAndPercentage() throws Exception {
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String creativeId = "creativeId";
        String dspId = "TEST_DSP_ID";
        String advId = "TEST_ADV_ID";
        String seatId = "TEST_SEAT_ID";
        String aqId = "TEST_AQ_ID";
        Set<Integer> packageIds = ImmutableSet.of(1,2,3);
        int winningPackageId = 2;
        String dealId = "TEST_DEAL_ID";
        String adStatus = "AD";
        int dst = DemandSourceType.IX.getValue();
        long campaignIncId = 123L;
        long adgroupIncId = 456L;
        long adIncId = 678L;
        long latency = 789L;
        double bidPriceInUSD = 4.0;
        double originalBidPriceInUSD = 5.0;
        ADCreativeType adCreativeType = ADCreativeType.BANNER;

        mockStaticNice(InspectorStats.class);
        IXAdNetwork mockIXAdNetwork = createMock(IXAdNetwork.class);
        AdNetworkInterface mockAdNetworkInterface = mockIXAdNetwork;
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);
        ChannelSegmentEntity mockChannelSegmentEntityForIncId = createMock(ChannelSegmentEntity.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        expect(mockSASRequestParameters.isSandBoxRequest()).andReturn(false).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(dst).anyTimes();
        expect(mockAdNetworkInterface.getLatency()).andReturn(latency).anyTimes();
        expect(mockAdNetworkInterface.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("Name").anyTimes();
        expect(mockAdNetworkInterface.getForwardedPackageIds()).andReturn(packageIds).anyTimes();
        expect(mockAdNetworkInterface.getDeal()).andReturn(DealEntity.newBuilder().id(dealId).packageId(winningPackageId).build()).anyTimes();
        expect(mockAdNetworkInterface.getShortlistedTargetingSegmentIds()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getForwardedDealIds()).andReturn(null).anyTimes();
        expect(mockIXAdNetwork.getDspId()).andReturn(dspId).times(2);
        expect(mockIXAdNetwork.getAdvId()).andReturn(advId).times(2);
        expect(mockIXAdNetwork.getSeatId()).andReturn(seatId).anyTimes();
        expect(mockIXAdNetwork.getAqid()).andReturn(aqId).times(2);
        expect(mockIXAdNetwork.getAdjustbid()).andReturn(0.8).times(2);
        expect(mockIXAdNetwork.getOriginalBidPriceInUsd()).andReturn(originalBidPriceInUSD).anyTimes();

        expect(mockThirdPartyAdResponse.getLatency()).andReturn(latency).anyTimes();
        expect(mockAdNetworkInterface.getAdStatus()).andReturn(adStatus).times(1);
        expect(mockThirdPartyAdResponse.getAdStatus()).andReturn(adStatus).times(2);
        expect(mockAdNetworkInterface.getCreativeId()).andReturn(creativeId).times(1).andReturn(null).anyTimes();

        expect(mockAdNetworkInterface.getCreativeType()).andReturn(adCreativeType).anyTimes();

        expect(mockIXAdNetwork.getEntity()).andReturn(mockChannelSegmentEntityForIncId).anyTimes();
        expect(mockIXAdNetwork.getHostName()).andReturn(DEFAULT_HOST).anyTimes();
        expect(mockChannelSegmentEntityForIncId.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntityForIncId.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();

        replayAll();

        assertThat(Logging.createChannelsLog(null, mockSASRequestParameters).size(), is(0));

        Channel channel = Logging.createChannelsLog(Arrays.asList(mockChannelSegment), mockSASRequestParameters).get(0);
        assertThat(channel.getAdStatus(), is(equalTo(Logging.getAdStatus(adStatus))));
        assertThat(channel.getLatency(), is(equalTo(latency)));
        CasAdChain casAdChain = channel.getAdChain();
        assertThat(casAdChain.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(casAdChain.getCampaign_inc_id(), is(equalTo(campaignIncId)));
        assertThat(casAdChain.getAdgroup_inc_id(), is(equalTo(adgroupIncId)));
        assertThat(casAdChain.getExternalSiteKey(), is(equalTo(externalSiteKey)));
        assertThat(casAdChain.getDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(casAdChain.getCreativeId(), is(equalTo(creativeId)));

        IxAd ixAd = channel.getDeprecatedIxAds().get(0);
        assertThat(ixAd.getDspId(), is(equalTo(dspId)));
        assertThat(ixAd.getAdvId(), is(equalTo(advId)));
        assertThat(ixAd.getSeatId(), is(equalTo(seatId)));
        assertThat(ixAd.getAqId(), is(equalTo(aqId)));
        assertTrue(CollectionUtils.isEqualCollection(ixAd.getDeprecatedPackageIds(),packageIds));
        assertThat(ixAd.getDeprecatedWinningPackageId(), is(equalTo(winningPackageId)));
        assertThat(ixAd.getDeprecatedWinningDealId(), is(equalTo(dealId)));
    }

    @Test
    public void testAdvertiserLoggingNotEnabled() throws Exception {
        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(false).anyTimes();
        replayAll();

        MemberModifier.stub(Logging.class.getDeclaredMethod("getLogger", String.class)).toReturn(mockLogger);
        Logging.advertiserLogging(null, mockConfig);
    }

    /**
     * Branches/Conditions followed:
     * AdStatus is "AD"
     * RequestUrl is not empty
     */
    @Test
    public void testAdvertiserLoggingEnabledVariation1() throws Exception {
        String expectedLogOutput = "Name\u0001AD\u0001HttpResponseContent\u0001RequestUrl";
        Logger mockLogger = createMock(Logger.class);
        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("Name").anyTimes();
        expect(mockAdNetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdNetworkInterface.getHttpResponseContent()).andReturn("HttpResponseContent").anyTimes();
        expect(mockAdNetworkInterface.getRequestUrl()).andReturn("RequestUrl").anyTimes();

        expect(mockThirdPartyAdResponse.getAdStatus()).andReturn("AD").anyTimes();

        mockLogger.debug(expectedLogOutput);
        expectLastCall().times(1);
        replayAll();

        MemberModifier.stub(Logging.class.getDeclaredMethod("getLogger", String.class)).toReturn(mockLogger);
        Logging.advertiserLogging(Arrays.asList(mockChannelSegment), mockConfig);
        verify(mockLogger);
    }

    /**
     * Branches/Conditions followed:
     * AdStatus is "NO_AD"
     * RequestUrl is empty
     */
    @Test
    public void testAdvertiserLoggingEnabledVariation2() throws Exception {
        String expectedLogOutput = "Name\u0001NO_AD";
        Logger mockLogger = createMock(Logger.class);
        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ThirdPartyAdResponse mockThirdPartyAdResponse = createMock(ThirdPartyAdResponse.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockAdNetworkInterface.getResponseStruct()).andReturn(mockThirdPartyAdResponse).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("Name").anyTimes();
        expect(mockAdNetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdNetworkInterface.getHttpResponseContent()).andReturn("HttpResponseContent").anyTimes();
        expect(mockAdNetworkInterface.getRequestUrl()).andReturn("").anyTimes();

        expect(mockThirdPartyAdResponse.getAdStatus()).andReturn("NO_AD").anyTimes();

        mockLogger.debug(expectedLogOutput);
        expectLastCall().times(1);
        replayAll();

        MemberModifier.stub(Logging.class.getDeclaredMethod("getLogger", String.class)).toReturn(mockLogger);
        Logging.advertiserLogging(Arrays.asList(mockChannelSegment), mockConfig);
        verify(mockLogger);
    }

    @Test
    public void testDecideToLog() throws Exception {
        Logging.SAMPLED_ADVERTISER_LOG_NOS.clear();
        for (int i = 1; i <= sampledadvertisercount + 1; ++i) {
            if (sampledadvertisercount + 1 == i) {
                assertThat(Logging.decideToLog("Partner", "ExternalSiteId"), is(false));
            } else {
                assertThat(Logging.decideToLog("Partner", "ExternalSiteId"), is(true));
            }
        }
        assertThat(Logging.SAMPLED_ADVERTISER_LOG_NOS.size(), is(1));
        assertThat(Logging.SAMPLED_ADVERTISER_LOG_NOS.get("PartnerExternalSiteId").split("_")[1], is("5"));
    }

    @Test
    public void testGetAdStatus() throws Exception {
        assertThat(Logging.getAdStatus(null), is(equalTo(AdStatus.DROPPED)));
        assertThat(Logging.getAdStatus("AD"), is(equalTo(AdStatus.AD)));
        assertThat(Logging.getAdStatus("NO_AD"), is(equalTo(AdStatus.NO_AD)));
        assertThat(Logging.getAdStatus("TIME_OUT"), is(equalTo(AdStatus.TIME_OUT)));
        assertThat(Logging.getAdStatus("SOMETHING_ELSE"), is(equalTo(AdStatus.DROPPED)));
    }

    @Test
    public void testGetContentRating() throws Exception {
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockSASRequestParameters.getSiteContentType()).andReturn(null).times(1)
                .andReturn(ContentType.PERFORMANCE).times(2).andReturn(ContentType.FAMILY_SAFE).times(2)
                .andReturn(ContentType.MATURE).times(2);
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();
        replayAll();

        assertThat(Logging.getContentRating(null), is(equalTo(null)));
        assertThat(Logging.getContentRating(mockSASRequestParameters), is(equalTo(null)));
        assertThat(Logging.getContentRating(mockSASRequestParameters), is(equalTo(ContentRating.PERFORMANCE)));
        assertThat(Logging.getContentRating(mockSASRequestParameters), is(equalTo(ContentRating.FAMILY_SAFE)));
        assertThat(Logging.getContentRating(mockSASRequestParameters), is(equalTo(ContentRating.MATURE)));
    }

    @Test
    public void testGetPricingModel() throws Exception {
        String[] pricingModel = new String[] {null, "cpc", "cpm", "fail"};
        PricingModel[] output = new PricingModel[] {null, PricingModel.CPC, PricingModel.CPM, null};
        for (int i = 0; i < pricingModel.length; ++i) {
            assertThat(Logging.getPricingModel(pricingModel[i]), is(equalTo(output[i])));
        }
    }

    @Test
    public void testGetInventoryType() throws Exception {
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockSASRequestParameters.getSdkVersion()).andReturn(null).times(1).andReturn("1").times(2).andReturn("0")
                .anyTimes();
        replayAll();

        assertThat(Logging.getInventoryType(null), is(equalTo(InventoryType.APP)));
        assertThat(Logging.getInventoryType(mockSASRequestParameters), is(equalTo(InventoryType.APP)));
        assertThat(Logging.getInventoryType(mockSASRequestParameters), is(equalTo(InventoryType.APP)));
        assertThat(Logging.getInventoryType(mockSASRequestParameters), is(equalTo(InventoryType.BROWSER)));
    }

    @Test
    public void testGetImpressionObjectNormal() throws Exception {
        ADCreativeType adCreativeType = ADCreativeType.BANNER;
        String adId = "adId";
        String adgroupId = "adgroupId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String impressionId = "impressionId";
        int dst = DemandSourceType.RTBD.getValue();
        Long campaignIncId = 123L;
        Long adgroupIncId = 123L;
        Long adIncId = 234L;
        Double secondBidPriceInUsd = 4.5;

        mockStaticNice(InspectorStats.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        AdNetworkInterface mockAdNetworkInterface = createMock(AdNetworkInterface.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockAdNetworkInterface).anyTimes();
        expect(mockAdNetworkInterface.getCreativeType()).andReturn(adCreativeType).anyTimes();
        expect(mockAdNetworkInterface.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUsd).anyTimes();
        expect(mockAdNetworkInterface.getCreativeId()).andReturn(null).anyTimes();
        expect(mockAdNetworkInterface.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockAdNetworkInterface.getName()).andReturn("name").anyTimes();
        expect(mockAdNetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdNetworkInterface.getDeal()).andReturn(null).anyTimes();

        expect(mockChannelSegmentEntity.getAdId(adCreativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adgroupId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(dst).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getSiteContentType()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();
        replayAll();

        Impression impression = Logging.getImpressionObject(mockChannelSegment, mockSASRequestParameters);
        Ad ad = impression.getAd();
        AdMeta adMeta = ad.getMeta();
        AdIdChain adIdChain = ad.getId();

        assertThat(impression.getImpressionId(), is(equalTo(impressionId)));
        assertThat(ad.getWinBid(), is(equalTo(secondBidPriceInUsd)));
        assertThat(adMeta.getCr(), is(equalTo(null)));
        assertThat(adMeta.getPricing(), is(equalTo(null)));
        assertThat(adIdChain.getAd(), is(equalTo(adId)));
        assertThat(adIdChain.getAdvid(), is(equalTo(advertiserId)));
        assertThat(adIdChain.getCampaign(), is(equalTo(campaignId)));
        assertThat(adIdChain.getExternal_site(), is(equalTo(externalSiteKey)));
        assertThat(adIdChain.getGroup(), is(equalTo(adgroupId)));
    }

    @Test
    public void testGetImpressionObjectIX() throws Exception {
        ADCreativeType adCreativeType = ADCreativeType.BANNER;
        String adId = "adId";
        String adgroupId = "adgroupId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String externalSiteKey = "externalSiteKey";
        String pricingModel = "cpm";
        String impressionId = "impressionId";
        int dst = DemandSourceType.IX.getValue();
        Long campaignIncId = 123L;
        Long adgroupIncId = 123L;
        Long adIncId = 567L;
        Double secondBidPriceInUsd = -1.0;
        ContentType siteContentType = ContentType.MATURE;
        mockStaticNice(InspectorStats.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        IXAdNetwork mockIXAdNetwork = createMock(IXAdNetwork.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockIXAdNetwork).anyTimes();
        expect(mockIXAdNetwork.getCreativeType()).andReturn(adCreativeType).anyTimes();
        expect(mockIXAdNetwork.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUsd).anyTimes();
        expect(mockIXAdNetwork.getCreativeId()).andReturn(null).anyTimes();
        expect(mockIXAdNetwork.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockIXAdNetwork.getName()).andReturn("name").anyTimes();
        expect(mockIXAdNetwork.getId()).andReturn("Id").anyTimes();
        expect(mockIXAdNetwork.getDeal()).andReturn(null).anyTimes();
        expect(mockIXAdNetwork.getForwardedDealIds()).andReturn(null).anyTimes();
        expect(mockChannelSegmentEntity.getAdId(adCreativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adgroupId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(adCreativeType)).andReturn(adIncId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(dst).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(pricingModel).anyTimes();
        expect(mockSASRequestParameters.getSiteContentType()).andReturn(siteContentType).anyTimes();
        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();
        replayAll();

        Logging.getImpressionObject(mockChannelSegment, mockSASRequestParameters);
    }

    @Test
    public void testGetImpressionObjectFailure() throws Exception {
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(null).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(null).anyTimes();
        replayAll();

        assertThat(Logging.getImpressionObject(null, null), is(equalTo(null)));
    }

    @Test
    public void testGetRequestObject() throws Exception {
        Request request;

        short adsServed = 5;
        Short requestSlot = 11;
        Short slotServed = 11;
        String siteId = "siteId";
        String taskId = "taskId";
        Integer siteSegmentIds = 45;
        Long countryId = 94L;
        Integer carrierId = 345;
        Integer state = 123;
        Integer city = 567;
        Short age = 11;
        String gender = "m";
        Long handsetInternalId = 123L;
        int osId = 3;
        int dst = DemandSourceType.IX.getValue();
        Double auctionBidFloor = 455.98;
        Double marketRate = 567.98;
        final String appBundleId = "testAppBundleId";
        final long placementId = 12l;
        final String iem = "dummy";

        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        IXAdNetwork mockIXAdNetwork = createMock(IXAdNetwork.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        CasInternalRequestParameters mockCasInternalRequestParameters = createMock(CasInternalRequestParameters.class);
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockIXAdNetwork).anyTimes();
        expect(mockIXAdNetwork.getForwardedBidFloor()).andReturn(auctionBidFloor).anyTimes();
        expect(mockIXAdNetwork.getForwardedBidGuidance()).andReturn(marketRate).anyTimes();
        expect(mockIXAdNetwork.getAppBundleId(false)).andReturn(appBundleId).anyTimes();
        expect(mockSASRequestParameters.getSiteId()).andReturn(siteId).anyTimes();
        expect(mockSASRequestParameters.getTid()).andReturn(taskId).anyTimes();
        expect(mockSASRequestParameters.getDst()).andReturn(dst).anyTimes();
        expect(mockSASRequestParameters.getSiteSegmentId()).andReturn(null).times(1).andReturn(siteSegmentIds)
                .anyTimes();

        expect(mockSASRequestParameters.getSdkVersion()).andReturn("0").anyTimes();
        expect(mockSASRequestParameters.getCountryId()).andReturn(countryId).anyTimes();
        expect(mockSASRequestParameters.getCarrierId()).andReturn(carrierId).anyTimes();
        expect(mockSASRequestParameters.getState()).andReturn(state).anyTimes();
        expect(mockSASRequestParameters.getCity()).andReturn(city).anyTimes();
        expect(mockSASRequestParameters.getAge()).andReturn(age).anyTimes();
        expect(mockSASRequestParameters.getGender()).andReturn(gender).anyTimes();
        expect(mockSASRequestParameters.getTUidParams()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getHandsetInternalId()).andReturn(handsetInternalId).anyTimes();
        expect(mockSASRequestParameters.getOsId()).andReturn(osId).anyTimes();
        expect(mockSASRequestParameters.getMarketRate()).andReturn(marketRate).anyTimes();
        expect(mockSASRequestParameters.getAppBundleId()).andReturn(appBundleId).anyTimes();
        expect(mockSASRequestParameters.getPlacementId()).andReturn(placementId).anyTimes();
        expect(mockSASRequestParameters.getIntegrationDetails()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getNormalizedUserId()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getRqMkSlot()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getNappScore()).andReturn(CONFIDENT_GOOD_SCORE).anyTimes();
        expect(mockSASRequestParameters.isBundleIdMismatched()).andReturn(true).anyTimes();
        expect(mockCasInternalRequestParameters.getImeiMD5()).andReturn(iem).anyTimes();
        expect(mockCasInternalRequestParameters.getImeiSHA1()).andReturn(iem).anyTimes();

        replayAll();
        List<ChannelSegment> rankList = Arrays.asList(mockChannelSegment);

        // sasParams are null
        request = Logging.getRequestObject(null, null, adsServed, requestSlot, slotServed, rankList);
        assertThat(request.isSetSite(), is(false));
        assertThat(request.isSetId(), is(false));
        assertThat(request.getSlot_served(), is(equalTo(slotServed)));
        assertThat(request.getSlot_requested(), is(equalTo(requestSlot)));
        assertThat(request.isSetSegmentId(), is(false));
        assertThat(request.isSetRequestDst(), is(false));
        assertThat(request.getAppBundleId(), is(equalTo(null)));
        assertThat(request.isSetPlacementId(), is(equalTo(false)));


        // sasParams is present, mockCasInternalRequestParameters is null, slotServed and requestSlot are null,
        // siteSegment  is null
        request = Logging.getRequestObject(mockSASRequestParameters, null, adsServed, null, null, rankList);
        assertThat(request.getSite(), is(siteId));
        assertThat(request.getId(), is(taskId));
        assertThat(request.isSetSlot_served(), is(false));
        assertThat(request.isSetSlot_requested(), is(false));
        assertThat(request.isSetSegmentId(), is(false));
        assertThat(request.getRequestDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(request.getRequestDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(request.getAuctionBidFloor(), is(equalTo(auctionBidFloor)));
        assertThat(request.getBidGuidance(), is(equalTo(marketRate)));
        assertThat(request.getPlacementId(), is(equalTo(placementId)));
        assertThat(request.isImeiPresent(), is(false));

        // everything is present
        request = Logging.getRequestObject(mockSASRequestParameters, mockCasInternalRequestParameters, adsServed,
                requestSlot, slotServed, rankList);
        assertThat(request.getSite(), is(siteId));
        assertThat(request.getId(), is(taskId));
        assertThat(request.getSlot_served(), is(equalTo(slotServed)));
        assertThat(request.getSlot_requested(), is(equalTo(requestSlot)));
        assertThat(request.getSegmentId(), is(siteSegmentIds));
        assertThat(request.getRequestDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(request.getRequestDst(), is(equalTo(DemandSourceType.findByValue(dst))));
        assertThat(request.getAuctionBidFloor(), is(equalTo(auctionBidFloor)));
        assertThat(request.getBidGuidance(), is(equalTo(marketRate)));
        assertThat(request.getPlacementId(), is(equalTo(placementId)));
        assertThat(request.isImeiPresent(), is(true));
    }

    @Test
    public void testGetGeoObject() throws Exception {
        Geo geo;

        Long countryId = 94L;
        Integer carrierId = 345;
        Integer state = 123;
        Integer city = 567;

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        expect(mockSASRequestParameters.getCountryId()).andReturn(null).times(1).andReturn(countryId).times(1)
                .andReturn(null).times(1).andReturn(countryId).anyTimes();
        expect(mockSASRequestParameters.getCarrierId()).andReturn(carrierId).times(1).andReturn(null).times(1)
                .andReturn(null).times(1).andReturn(carrierId).anyTimes();
        expect(mockSASRequestParameters.getState()).andReturn(null).times(4).andReturn(state).anyTimes();
        expect(mockSASRequestParameters.getCity()).andReturn(null).times(4).andReturn(city).anyTimes();
        replayAll();

        geo = Logging.getGeoObject(null);
        assertThat(geo, is(equalTo(null)));

        geo = Logging.getGeoObject(mockSASRequestParameters);
        assertThat(geo, is(equalTo(null)));

        geo = Logging.getGeoObject(mockSASRequestParameters);
        assertThat(geo, is(equalTo(null)));

        geo = Logging.getGeoObject(mockSASRequestParameters);
        assertThat(geo, is(equalTo(null)));

        geo = Logging.getGeoObject(mockSASRequestParameters);
        assertThat(geo.getCarrier(), is(carrierId));
        assertThat(geo.getCountry(), is(countryId.shortValue()));
        assertThat(geo.isSetCity(), is(false));
        assertThat(geo.isSetRegion(), is(false));

        geo = Logging.getGeoObject(mockSASRequestParameters);
        assertThat(geo.getCarrier(), is(carrierId));
        assertThat(geo.getCountry(), is(countryId.shortValue()));
        assertThat(geo.getCity(), is(city));
        assertThat(geo.getRegion(), is(state));
    }

    @Test
    public void testGetUserObject() throws Exception {
        User user;

        Short age = 11;
        String gender = "m";
        final String normaliseduserId = "n-uid";

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        expect(mockSASRequestParameters.getAge()).andReturn(age).anyTimes();
        expect(mockSASRequestParameters.getGender()).andReturn(gender).anyTimes();
        expect(mockSASRequestParameters.getTUidParams()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getNormalizedUserId()).andReturn(normaliseduserId).anyTimes();
        replayAll();

        user = Logging.getUserObject(null);
        assertThat(user.isSetAge(), is(false));
        assertThat(user.isSetGender(), is(false));
        assertThat(user.isSetNormalized_user_id(), is(false));

        user = Logging.getUserObject(mockSASRequestParameters);
        assertThat(user.getAge(), is(equalTo(age)));
        assertThat(user.getGender(), is(equalTo(Gender.MALE)));
        assertThat(user.getNormalized_user_id(), is(equalTo(normaliseduserId)));
    }

    @Test
    public void testGetHandsetMetaObject() throws Exception {
        HandsetMeta handsetMeta;
        Long handsetInternalId = 123L;
        int osId = 3;

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        expect(mockSASRequestParameters.getHandsetInternalId()).andReturn(handsetInternalId).anyTimes();
        expect(mockSASRequestParameters.getOsId()).andReturn(osId).anyTimes();
        replayAll();

        handsetMeta = Logging.getHandsetMetaObject(null);
        assertThat(handsetMeta.isSetId(), is(false));
        assertThat(handsetMeta.isSetOsId(), is(false));

        handsetMeta = Logging.getHandsetMetaObject(mockSASRequestParameters);
        assertThat(handsetMeta.getId(), is(equalTo(handsetInternalId.intValue())));
        assertThat(handsetMeta.getOsId(), is(equalTo(osId)));
    }

    @Test
    public void testGetGender() throws Exception {
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockSASRequestParameters.getGender()).andReturn("m").times(1).andReturn("f").anyTimes();
        replayAll();

        assertThat(Logging.getGender(null), is(equalTo(null)));
        assertThat(Logging.getGender(mockSASRequestParameters), is(equalTo(Gender.MALE)));
        assertThat(Logging.getGender(mockSASRequestParameters), is(equalTo(Gender.FEMALE)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testsampledAdvertisingLogging() {
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(ChannelSegmentFilterApplierTest
                .getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId", "channelId", 1, rcList, tags, true,
                        true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel",
                        siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        final List<ChannelSegment> rankList = createMock(ArrayList.class);
        final AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        final ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.setAdStatus("AD");
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork1").anyTimes();
        expect(mockAdnetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("DummyResponsecontent").anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();

        final ChannelSegment channelSegment =
                new ChannelSegment(channelSegmentEntity, null, null, null, null, mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(1).anyTimes();
        replayAll();
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testsampledAdvertisingLoggingWithResponseAsEmptyString() {
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(ChannelSegmentFilterApplierTest
                .getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId", "channelId", 1, rcList, tags, true,
                        true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel",
                        siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        final List<ChannelSegment> rankList = createMock(ArrayList.class);
        final AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        final ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.setAdStatus("AD");
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
        expect(mockAdnetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("").anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();

        final ChannelSegment channelSegment =
                new ChannelSegment(channelSegmentEntity, null, null, null, null, mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(1).anyTimes();
        replayAll();
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testsampledAdvertisingLoggingWithRequestUrlAsEmptyString() {
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(ChannelSegmentFilterApplierTest
                .getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId", "channelId", 1, rcList, tags, true,
                        true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel",
                        siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        final List<ChannelSegment> rankList = createMock(ArrayList.class);
        final AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        final ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.setAdStatus("AD");
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
        expect(mockAdnetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("").anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();

        final ChannelSegment channelSegment =
                new ChannelSegment(channelSegmentEntity, null, null, null, null, mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(1).anyTimes();
        replayAll();
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testsampledAdvertisingLoggingForZeroSampleOnDatabus() {
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(ChannelSegmentFilterApplierTest
                .getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId", "channelId", 1, rcList, tags, true,
                        true, "externalSiteKey", modified_on, "campaignId", slotIds, 1, true, "pricingModel",
                        siteRatings, 1, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<>(), 0.0d, null, null, false, emptySet, 0));
        final List<ChannelSegment> rankList = createMock(ArrayList.class);
        final AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        final ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.setAdStatus("AD");
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
        expect(mockAdnetworkInterface.getId()).andReturn("Id").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("response").anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();

        final ChannelSegment channelSegment =
                new ChannelSegment(channelSegmentEntity, null, null, null, null, mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.get(1)).andReturn(channelSegment).anyTimes();
        expect(rankList.get(2)).andReturn(channelSegment).anyTimes();
        expect(rankList.get(3)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(4).anyTimes();
        replayAll();
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }
}
