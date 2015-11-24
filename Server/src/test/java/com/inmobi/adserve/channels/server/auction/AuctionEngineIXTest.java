package com.inmobi.adserve.channels.server.auction;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replay;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
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
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(AuctionEngineHelper.class)
public class AuctionEngineIXTest extends PowerMockTestCase {

    Configuration mockConfig;
    Capture<String> encryptedBid1;
    Capture<Double> secondPrice1;
    CasInternalRequestParameters casInternalRequestParameters;
    AuctionFilterApplier auctionFilterApplier;
    AuctionEngine auctionEngine;
    SASRequestParameters sasParams;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() throws IOException, IllegalAccessException {
        mockStaticNice(AuctionEngineHelper.class);
        replay(AuctionEngineHelper.class);

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
                Guice.createInjector(Modules.override(new ServerModule(config, repositoryHelper, "containerName"),
                        new CasNettyModule(config.getServerConfiguration())).with(new TestScopeModule()));
        auctionFilterApplier = injector.getInstance(AuctionFilterApplier.class);
        auctionEngine = injector.getInstance(AuctionEngine.class);
    }

    @SuppressWarnings("deprecation")
    private ChannelSegment setBidder(final String advId, final String channelId, final String externalSiteKey,
            final String adNetworkName, final Double bidValue, final Long latencyValue,
            final ADCreativeType adCreativeType, final boolean isTrumpDeal) {

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
        expect(((IXAdNetwork) mockAdnetworkInterface).getAgencyRebatePercentage()).andReturn(null).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).getOriginalBidPriceInUsd()).andReturn(bidValue).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).getImpressionObjCount()).andReturn(1).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).isTrumpDeal()).andReturn(isTrumpDeal).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).isVideoRequest()).andReturn(true).anyTimes();
        expect(((IXAdNetwork) mockAdnetworkInterface).getDealId()).andReturn("dealId").anyTimes();
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
            expect(((IXAdNetwork) mockAdnetworkInterface).getDspChannelSegmentEntity())
                    .andReturn(channelSegmentEntity1).times(1);
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
                ADCreativeType.BANNER, false));
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
                ADCreativeType.BANNER, false));

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

        rtbSegments
            .add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1, ADCreativeType.BANNER, false));

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
                ADCreativeType.INTERSTITIAL_VIDEO, false));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.setAuctionBidFloor(bidFloorInput);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceVal);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBAdNetworkName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), bidInputVal1);
    }

    // This function will provide the parameter data
    @DataProvider(name = "DataProviderWith3Bidders")
    public Object[][] paramDataProviderWith3Bidders() {
        return new Object[][] {
            // 1. 2 same first bids, higher than floor price
            {"testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .80d, 100l, false, "C", .80d, 150l, false, "A", 1d},

            // 2. 2 same first bids, with same latency, higher than floor price
            {"testTwoSameSecondBidsWithSameLatencyHigherThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .80d, 100l, false, "C", .80d, 100l, false, "A", 1d},

            // 3. 2 same first bids, lower than floor price
            {"testTwoSameSecondBidsLowerThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .60d, 100l, false, "C", .60d, 150l, false, "A", 1d},

            // 4. 2 same first bids, lower than 90% of Highest bid but more than floor price
            {"testTwoSameSecondBidsLowerThan90PercentOfHighestBidButMoreThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .85d, 100l, false, "C", .85d, 150l, false, "A", 1d},

            // 5. 2 same first bids, lower than 90% of Highest bid and lower than floor price
            {"testTwoSameSecondBidsLowerThan90PercentOfHighestBidAndLowerThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .65d, 100l, false, "C", .65d, 150l, false, "A", 1d},

            // 6. 2 same first bids, higher than 90% of Highest bid but more than floor price
            {"testTwoSameSecondBidsHigherThan90PercentOfHighestBidButMoreThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .95d, 100l, false, "C", .95d, 150l, false, "A", 1d},

            // 7. 2 same first bids, higher than 90% of Highest bid and lower than floor price
            {"testTwoSameSecondBidsHigherThan90PercentOfHighestBidButLowerThanFloorPrice", .98d, "A", 1d, 120l, false, "B", .95d, 100l, false, "C", .95d, 150l, false, "A", 1d},

            // 8. First and Second bid with .01 difference. He will be charged same Bid he auctioned
            {"testFirstAndSecondBidWith01difference", .70d, "A", 1d, 100l, false, "B", .99d, 100l, false, "C", 0.99d, 100l, false, "A", 1d},

            // 9. 2nd price same as floor price
            {"testSecondPriceSameAsFloorPrice", .70d, "A", 1d, 100l, false, "B", .70d, 100l, false, "C", .70d, 100l, false, "A", 1d},

            // 10. 2nd price same as floor price and 90% price
            {"testSecondPriceSameAsFloorPriceAnd90PercentPrice", .90d, "A", 1d, 100l, false, "B", .90d, 100l, false, "C", .70d, 100l, false, "A", 1d},

            // 11. 2 same first bids, higher than floor price. First has trump deal
            {"testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, true, "B", .80d, 100l, false, "C", .80d, 150l, false, "A", 1d},

            // 12. 2 same first bids, higher than floor price. Second has trump deal
            {"testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .80d, 100l, true, "C", .80d, 150l, false, "B", .80d},

            // 13. 2 same first bids, higher than floor price. Third has trump deal
            {"testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, false, "B", .80d, 100l, false, "C", .80d, 150l, true, "C", .80d},

            // 14. 3 different first bids, higher than floor price. First and Second have trump deal
            {"testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, true, "B", 1.80d, 100l, true, "C", .80d, 150l, false, "B", 1.80d},

            // 15. 2 same first bids, higher than floor price. All three have trump deal
            {"testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, true, "B", 1.80d, 190l, true, "C", 1.80d, 150l, true, "C", 1.80d}

        };
    }

    @Test(dataProvider = "DataProviderWith3Bidders")
    public void testAuctionEngineWith3BiddersExample(final String useCaseName, final Double floorPrice,
        final String ixNameInput1, final Double bidInput1, final Long latencyInput1, final boolean hasTrumpDeal1, final String ixNameInput2,
        final Double bidInput2, final Long latencyInput2, final boolean hasTrumpDeal2, final String ixNameInput3, final Double bidInput3,
        final Long latencyInput3, final boolean hasTrumpDeal3, final String expectedIXName, final Double expectedWinnerBidValue) {

        final AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.IX.getValue());
        final List<ChannelSegment> ixSegments = new ArrayList<>();

        ixSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", ixNameInput1, bidInput1, latencyInput1, ADCreativeType.INTERSTITIAL_VIDEO, hasTrumpDeal1));
        ixSegments.add(setBidder("advId2", "channelId2", "externalSiteKey2", ixNameInput2, bidInput2, latencyInput2, ADCreativeType.INTERSTITIAL_VIDEO, hasTrumpDeal2));
        ixSegments.add(setBidder("advId3", "channelId3", "externalSiteKey3", ixNameInput3, bidInput3, latencyInput3, ADCreativeType.INTERSTITIAL_VIDEO, hasTrumpDeal3));
        auctionEngine.setUnfilteredChannelSegmentList(ixSegments);

        casInternalRequestParameters.setAuctionBidFloor(floorPrice);
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        final AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(auctionEngineResponse.getName(), expectedIXName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), expectedWinnerBidValue);
    }
}
