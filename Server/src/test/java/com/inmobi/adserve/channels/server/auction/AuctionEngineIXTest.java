package com.inmobi.adserve.channels.server.auction;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.powermock.api.support.membermodification.MemberModifier;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplierTest;
import com.inmobi.adserve.channels.server.requesthandler.filters.TestScopeModule;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;

public class AuctionEngineIXTest {

    Configuration mockConfig;
    Configuration mockAdapterConfig;
    Capture<String> encryptedBid1;
    Capture<Double> secondPrice1;
    CasInternalRequestParameters casInternalRequestParameters;
    AuctionFilterApplier auctionFilterApplier;
    AuctionEngine auctionEngine;
    SASRequestParameters sasParams;

    @BeforeMethod
    public void setUp() throws IOException, IllegalAccessException {

        MemberModifier.field(InspectorStats.class, "boxName")
                .set(InspectorStats.class, "randomBox");

        final ConfigurationLoader config = ConfigurationLoader.getInstance("channel-server.properties");
        CasConfigUtil.init(config, null);

        ImpressionIdGenerator.init((short) 123, (byte) 10);

        // this is done, to track the encryptedBid variable getting set inside the AuctionEngine.
        encryptedBid1 = new Capture<String>();

        // this is done, to track the secondBidPrice variable getting set inside the AuctionEngine.
        secondPrice1 = new Capture<Double>();
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getInt("totalSegmentNo")).andReturn(5).anyTimes();
        expect(mockConfig.getDouble("revenueWindow", 0.33)).andReturn(10.0).anyTimes();
        expect(mockConfig.getDouble("ecpmShift", 0.1)).andReturn(0.0).anyTimes();
        expect(mockConfig.getDouble("feedbackPower", 2.0)).andReturn(1.0).anyTimes();
        expect(mockConfig.getInt("partnerSegmentNo", 2)).andReturn(2).anyTimes();
        expect(mockConfig.getInt("whiteListedSitesRefreshtime", 1000 * 300)).andReturn(0).anyTimes();
        expect(mockConfig.getInt("rtb.maxconnections")).andReturn(50).anyTimes();

        replay(mockConfig);

        sasParams = new SASRequestParameters();
        sasParams.setDst(DemandSourceType.IX.getValue());
        sasParams.setCountryId(94L);
        sasParams.setCarrierId(1);
        sasParams.setSiteId("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams.setDst(DemandSourceType.IX.getValue());

        casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setAuctionId("auctionId");
        casInternalRequestParameters.setSiteAccountType(AccountType.SELF_SERVE);
        final RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryCreativeRepository(EasyMock.isA(String.class), EasyMock.isA(String.class)))
                .andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        replay(repositoryHelper);

        final Injector injector =
                Guice.createInjector(Modules.override(new ServerModule(config, repositoryHelper),
                        new CasNettyModule(config.getServerConfiguration())).with(new TestScopeModule()));
        auctionFilterApplier = injector.getInstance(AuctionFilterApplier.class);
        auctionEngine = injector.getInstance(AuctionEngine.class);
    }

    @SuppressWarnings("deprecation")
    private ChannelSegment setBidder(final String advId, final String channelId, final String externalSiteKey,
                                     final String adNetworkName, final Double bidValue, final Long latencyValue,
                                     final ADCreativeType adCreativeType) {

        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity1 =
                new ChannelSegmentEntity(ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder(advId,
                        "adgroupId", "adId", channelId, 1, rcList, tags, true, true, externalSiteKey, modified_on,
                        "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false,
                        false, false, false, false, false, false, false, null, null, 0.0d, null, null, false,
                        new HashSet<String>(), 0));
        final ChannelEntity.Builder builder = ChannelEntity.newBuilder();
        builder.setAccountId(advId);
        final ChannelEntity channelEntity = builder.build();

        final AdNetworkInterface mockAdnetworkInterface = createMock(IXAdNetwork.class);
        final ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.setAdStatus("AD");
        expect(mockAdnetworkInterface.getAdStatus()).andReturn("AD").anyTimes();
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getSecondBidPriceInUsd()).andReturn(4d).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn(adNetworkName).anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("DummyResponsecontent").anyTimes();
        expect(mockAdnetworkInterface.getConnectionLatency()).andReturn(2l).anyTimes();
        expect(mockAdnetworkInterface.getId()).andReturn("2").anyTimes();
        expect(mockAdnetworkInterface.getLatency()).andReturn(latencyValue).anyTimes();
        expect(mockAdnetworkInterface.getBidPriceInUsd()).andReturn(bidValue).anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(true).anyTimes();
        expect(mockAdnetworkInterface.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(mockAdnetworkInterface.getRtbImpressionId()).andReturn("1").anyTimes();
        expect(mockAdnetworkInterface.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(mockAdnetworkInterface.getSeatId()).andReturn(advId).anyTimes();
        expect(mockAdnetworkInterface.getCurrency()).andReturn("USD").anyTimes();
        expect(mockAdnetworkInterface.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(mockAdnetworkInterface.getDst()).andReturn(DemandSourceType.IX).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).getResponseBidObjCount()).andReturn(1).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).getImpressionObjCount()).andReturn(1).anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(adCreativeType).anyTimes();
        // responseBidObjCount
        // this is done, to track the encryptedBid variable getting set inside the AuctionEngine.
        mockAdnetworkInterface.setEncryptedBid(EasyMock.capture(encryptedBid1));
        EasyMock.expectLastCall().anyTimes();

        // this is done, to track the secondBidPrice variable getting set inside the AuctionEngine.
        mockAdnetworkInterface.setSecondBidPrice(EasyMock.capture(secondPrice1));
        EasyMock.expectLastCall().anyTimes();

        // Only for video ads, setLogCreative() should be set to true.
        if (adCreativeType == ADCreativeType.INTERSTITIAL_VIDEO) {
            expect(((IXAdNetwork) mockAdnetworkInterface).getDspChannelSegmentEntity()).andReturn(channelSegmentEntity1)
                    .times(1);
            mockAdnetworkInterface.setLogCreative(true);
            EasyMock.expectLastCall().times(1);
        }
        replay(mockAdnetworkInterface);

        return new ChannelSegment(channelSegmentEntity1, channelEntity, null, null, null, mockAdnetworkInterface, 0);
    }

    @Test
    // 1. 1 IX bid
    public void testOneBid() {

        final Double bidFloorInput = .70d;
        final Double bidInputVal1 = 1d;
        final Long latencyInputVal1 = 100l;
        final Double expectedSecondPriceVal = 1d;
        final String expectedRTBAdNetworkName = "A";

        final AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = sasParams;
        final List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1,
                ADCreativeType.BANNER));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.setAuctionBidFloor(bidFloorInput);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceVal);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBAdNetworkName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), bidInputVal1);
    }

    @Test
    // 2. 1 IX bid with price 0.1 above floor price
    public void testOneBidWithPrice1AboveFloorPrice() {

        final Double bidFloorInput = .70d;
        final Double bidInputVal1 = .71d;
        final Long latencyInputVal1 = 100l;
        final Double expectedSecondPriceVal = .71d;
        final String expectedRTBAdNetworkName = "A";

        final AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = this.sasParams;
        final List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1,
                ADCreativeType.BANNER));

        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.setAuctionBidFloor(bidFloorInput);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceVal);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBAdNetworkName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), bidInputVal1);
    }

    @Test
    // 3. Only 1 bid with price 0.1 below floor price. this bid will get
    // directly cut off at floor price filter
    public void testOneBidWithPrice1BelowFloorPrice() {

        final Double bidFloorInput = .70d;
        final Double bidInputVal1 = .69d;
        final Long latencyInputVal1 = 100l;
        final String expectedAuctionEngineResponse = null;

        final AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = this.sasParams;
        final List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1,
                ADCreativeType.BANNER));

        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.setAuctionBidFloor(bidFloorInput);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        try {
            secondPrice1.getValue();
            Assert.fail("Value is not supposed to set.");
        } catch (final AssertionError er) {

        }

        Assert.assertEquals(auctionEngineResponse, expectedAuctionEngineResponse);
    }

    @Test
    // 4. 0 IX bid
    public void testZeroBid() {

        final Double bidFloorInput = .70d;
        final String expectedAuctionEngineResponse = null;

        final AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = this.sasParams;
        final List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.setAuctionBidFloor(bidFloorInput);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();
        Assert.assertEquals(auctionEngineResponse, expectedAuctionEngineResponse);
    }

    @Test
    // 5. IX video Ad
    public void testOneVideoBidShouldLogCreative() {
        final Double bidFloorInput = .70d;
        final Double bidInputVal1 = 1d;
        final Long latencyInputVal1 = 100l;
        final Double expectedSecondPriceVal = 1d;
        final String expectedRTBAdNetworkName = "A";

        final AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = sasParams;
        final List<ChannelSegment> rtbSegments = new ArrayList<>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1,
                ADCreativeType.INTERSTITIAL_VIDEO));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.setAuctionBidFloor(bidFloorInput);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceVal);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBAdNetworkName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), bidInputVal1);
    }
}
