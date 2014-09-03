package com.inmobi.adserve.channels.server.auction;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
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
import com.inmobi.casthrift.DemandSourceType;
import org.apache.commons.configuration.Configuration;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class AuctionEngineTest {

    Configuration                mockConfig;
    Configuration                mockAdapterConfig;
    Capture<String>              encryptedBid1;
    Capture<Double>              secondPrice1;
    CasInternalRequestParameters casInternalRequestParameters;
    AuctionFilterApplier         auctionFilterApplier;
    AuctionEngine auctionEngine;

    @BeforeMethod
    public void setUp() throws IOException {
        final ConfigurationLoader config = ConfigurationLoader.getInstance("channel-server.properties");
        CasConfigUtil.init(config, null);
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
        expect(mockConfig.getInt("RTBreadtimeoutMillis")).andReturn(200).anyTimes();
        expect(mockConfig.getBoolean("isRtbEnabled")).andReturn(true).anyTimes();
        expect(mockConfig.getInt("rtb.maxconnections")).andReturn(50).anyTimes();

        replay(mockConfig);

        casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryCreativeRepository(EasyMock.isA(String.class), EasyMock.isA(String.class))).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        replay(repositoryHelper);

        Injector injector = Guice.createInjector(Modules.override(new ServerModule(config, repositoryHelper),
                new CasNettyModule(config.getServerConfiguration())).with(new TestScopeModule()));
        auctionFilterApplier = injector.getInstance(AuctionFilterApplier.class);
        auctionEngine = injector.getInstance(AuctionEngine.class);


    }

    private ChannelSegment setBidder(final String advId, final String channelId, final String externalSiteKey,
                                     final String adNetworkName, final Double bidValue, final Long latencyValue) {

        Long[] rcList = null;
        Long[] tags = null;
        Timestamp modified_on = null;
        Long[] slotIds = null;
        Integer[] siteRatings = null;
        ChannelSegmentEntity channelSegmentEntity1 = new ChannelSegmentEntity(
                ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder(advId, "adgroupId", "adId", channelId,
                        1, rcList, tags, true, true, externalSiteKey, modified_on, "campaignId", slotIds, 1, true,
                        "pricingModel", siteRatings, 1, null, false, false, false, false, false, false, false, false,
                        false, false, null, null, 0.0d, null, null, false, new HashSet<String>(), 0));
        ChannelEntity.Builder builder = ChannelEntity.newBuilder();
        builder.setAccountId(advId);
        ChannelEntity channelEntity = builder.build();

        AdNetworkInterface mockAdnetworkInterface = createMock(RtbAdNetwork.class);
        ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.adStatus = "AD";
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
        expect(mockAdnetworkInterface.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(mockAdnetworkInterface.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(mockAdnetworkInterface.getSeatId()).andReturn(advId).anyTimes();
        expect(mockAdnetworkInterface.getCurrency()).andReturn("USD").anyTimes();
        expect(mockAdnetworkInterface.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(mockAdnetworkInterface.getDst()).andReturn(6).anyTimes();

        mockAdnetworkInterface.setLogCreative(true);
        EasyMock.expectLastCall().anyTimes();
        List<String> adomains = new ArrayList<String>();
        adomains.add("a.com");
        expect(mockAdnetworkInterface.getADomain()).andReturn(adomains).anyTimes();
        List<Integer> cAttributes = new ArrayList<Integer>();
        cAttributes.add(1);
        expect(mockAdnetworkInterface.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(mockAdnetworkInterface.getIUrl()).andReturn("iurl").anyTimes();

        // this is done, to track the encryptedBid variable getting set inside the AuctionEngine.
        mockAdnetworkInterface.setEncryptedBid(EasyMock.capture(encryptedBid1));
        EasyMock.expectLastCall().anyTimes();

        // this is done, to track the secondBidPrice variable getting set inside the AuctionEngine.
        mockAdnetworkInterface.setSecondBidPrice(EasyMock.capture(secondPrice1));
        EasyMock.expectLastCall().anyTimes();

        replay(mockAdnetworkInterface);

        return new ChannelSegment(channelSegmentEntity1, channelEntity, null, null, null, mockAdnetworkInterface, 0);

    }

    // This function will provide the parameter data
    @DataProvider(name = "DataProviderWith3Bidders")
    public Object[][] paramDataProviderWith3Bidders() {
        return new Object[][] {
                // 9. 2 same second bids, higher than floor price
                { "testTwoSameBidsHigherThanFloorPrice", .70d, "A", 1d, 120l, "B", .80d, 100l, "C", .80d, 150l, .80d,
                        "A", 1d },

                // 10. 2 same second bids, with same latency, higher than floor price
                { "testTwoSameSecondBidsWithSameLatencyHigherThanFloorPrice", .70d, "A", 1d, 120l, "B", .80d, 100l,
                        "C", .80d, 100l, .80d, "A", 1d },

                // 11. 2 same second bids, lower than floor price
                { "testTwoSameSecondBidsLowerThanFloorPrice", .70d, "A", 1d, 120l, "B", .60d, 100l, "C", .60d, 150l,
                        .70d, "A", 1d },

                // 12. 2 same second bids, lower than 90% of Highest bid but more than floor price
                { "testTwoSameSecondBidsLowerThan90PercentOfHighestBidButMoreThanFloorPrice", .70d, "A", 1d, 120l, "B",
                        .85d, 100l, "C", .85d, 150l, .85d, "A", 1d },

                // 13. 2 same second bids, lower than 90% of Highest bid and lower than floor price
                { "testTwoSameSecondBidsLowerThan90PercentOfHighestBidAndLowerThanFloorPrice", .70d, "A", 1d, 120l,
                        "B", .65d, 100l, "C", .65d, 150l, .70d, "A", 1d },

                // 14. 2 same second bids, higher than 90% of Highest bid but more than floor price
                { "testTwoSameSecondBidsHigherThan90PercentOfHighestBidButMoreThanFloorPrice", .70d, "A", 1d, 120l,
                        "B", .95d, 100l, "C", .95d, 150l, .95d, "A", 1d },

                // 15. 2 same second bids, higher than 90% of Highest bid and lower than floor price
                { "testTwoSameSecondBidsHigherThan90PercentOfHighestBidButLowerThanFloorPrice", .98d, "A", 1d, 120l,
                        "B", .95d, 100l, "C", .95d, 150l, .98d, "A", 1d },

                // 18. First and Second bid with .01 difference. He will be charged same Bid he auctioned
                { "testFirstAndSecondBidWith01difference", .70d, "A", 1d, 100l, "B", .99d, 100l, "C", .70d, 100l,
                        0.99d, "A", 1d },

                // 19. 2nd price same as floor price
                { "testSecondPriceSameAsFloorPrice", .70d, "A", 1d, 100l, "B", .70d, 100l, "C", .70d, 100l, .70d, "A",
                        1d },

                // 20. 2nd price same as floor price and 90% price
                { "testSecondPriceSameAsFloorPriceAnd90PercentPrice", .90d, "A", 1d, 100l, "B", .90d, 100l, "C", .70d,
                        100l, .90d, "A", 1d }

        };
    }

    @Test(dataProvider = "DataProviderWith3Bidders")
    public void testAuctionEngineWith3BiddersExample(final String useCaseName, final Double floorPrice,
                                                     final String rtbNameInput1, final Double bidInput1, final Long latencyInput1, final String rtbNameInput2,
                                                     final Double bidInput2, final Long latencyInput2, final String rtbNameInput3, final Double bidInput3,
                                                     final Long latencyInput3, final Double expectedSecondPriceValue, final String expectedRTBName,
                                                     final Double expectedWinnerBidValue) {

        AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.RTBD.getValue());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", rtbNameInput1, bidInput1, latencyInput1));
        rtbSegments.add(setBidder("advId2", "channelId2", "externalSiteKey2", rtbNameInput2, bidInput2, latencyInput2));
        rtbSegments.add(setBidder("advId3", "channelId3", "externalSiteKey3", rtbNameInput3, bidInput3, latencyInput3));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.auctionBidFloor = floorPrice;
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceValue);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), expectedWinnerBidValue);

    }

    // This function will provide the parameter data
    @DataProvider(name = "DataProviderWith4Bidders")
    public Object[][] paramDataProviderWith4Bidders() {
        return new Object[][] {
                // 1. more than floor price, 2nd price
                { "testBidHigherThanFloorPriceAndSecondPrice", .70d, "A", 1d, 50l, "B", .90d, 100l, "C", .80d, 150l,
                        "D", .70d, 100l, .90d, "A", 1d },

                // 2. same first bid price
                { "testSameFirstBidPrice", .70d, "A", 1d, 100l, "B", 1d, 75l, "C", 1d, 50l, "D", .70d, 100l, .70d, "C",
                        1d },

                // 3. same second bid price
                { "testSameSecondBidPrice", .70d, "A", 1d, 100l, "B", .80d, 50l, "C", .80d, 150l, "D", .70d, 100l,
                        .80d, "A", 1d },

                // 4. 2 win bid with same latency
                { "testTwoWinBidsWithSameLantency", .70d, "A", 1d, 100l, "B", 1d, 100l, "C", .80d, 150l, "D", .70d,
                        100l, .80d, "A", 1d },

                // 6. lower than floor price, the second bid onwards
                { "testBidLowerThanFloorPriceFromSecondBidOnwards", .70d, "A", 1d, 100l, "B", .40d, 100l, "C", .20d,
                        150l, "D", .45d, 100l, .70d, "A", 1d },

                // 8. all same bids, then latency is considered
                { "testAllSameBidsWithDifferentLatency", .70d, "A", 1d, 120l, "B", 1d, 100l, "C", 1d, 150l, "D", 1d,
                        70l, .70d, "D", 1d },

                // 16. 2 same first bid with 2nd Highest bid with 0.01 difference
                { "testTwoSameFirstBidWithSecondHighestBidWith01difference", .70d, "A", 1d, 100l, "B", 1d, 100l, "C",
                        .99d, 150l, "D", .70d, 100l, 0.99d, "A", 1d },

                // 17. First and Second bid with .01 difference and double 2nd entry. He will charged sameBid he
                // auctioned
                { "testFirstAndSecondBidWith01differenceAndDoubleSecondEntry", .70d, "A", 1d, 100l, "B", .99d, 100l,
                        "C", .99d, 150l, "D", .70, 100l, 0.99d, "A", 1d },

                // 21. 2 Highest price, and 2 same Second price
                { "testSecondHighestPriceAndTwoSameSecondPrice", .70d, "A", 1d, 100l, "B", 1d, 100l, "C", .90d, 150l,
                        "D", .90d, 100l, .90d, "A", 1d },

                // 22. 2 Highest price, and 2 same Second price, lower than floor price
                { "testTwoSecondHighestPriceAndTwoSameSecondPriceButLowerThanFloorPrice", .70d, "A", 1d, 100l, "B", 1d,
                        100l, "C", .60d, 150l, "D", .60d, 100l, .70d, "A", 1d },

                // 23. 2nd price is 0.1 above floor price
                { "testSecondPriceIs1AboveFloorPrice", .70d, "A", .71d, 100l, "B", 1d, 100l, "C", .60d, 150l, "D",
                        .60d, 100l, .71d, "B", 1d },

                // 24. 2nd price is 0.1 below floor price. this bid will get directly cut off at floor price filter
                { "testSecondPriceIs1BelowFloorPrice", .70d, "A", 1d, 100l, "B", .69d, 100l, "C", .60d, 150l, "D",
                        .60d, 100l, .70d, "A", 1d }

        };
    }

    @Test(dataProvider = "DataProviderWith4Bidders")
    public void testAuctionEngineWith4BiddersExample(final String useCaseName, final Double floorPrice,
                                                     final String rtbNameInput1, final Double bidInput1, final Long latencyInput1, final String rtbNameInput2,
                                                     final Double bidInput2, final Long latencyInput2, final String rtbNameInput3, final Double bidInput3,
                                                     final Long latencyInput3, final String rtbNameInput4, final Double bidInput4, final Long latencyInput4,
                                                     final Double expectedSecondPriceValue, final String expectedRTBName, final Double expectedWinnerBidValue) {

        AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.RTBD.getValue());

        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", rtbNameInput1, bidInput1, latencyInput1));
        rtbSegments.add(setBidder("advId2", "channelId2", "externalSiteKey2", rtbNameInput2, bidInput2, latencyInput2));
        rtbSegments.add(setBidder("advId3", "channelId3", "externalSiteKey3", rtbNameInput3, bidInput3, latencyInput3));
        rtbSegments.add(setBidder("advId4", "channelId4", "externalSiteKey4", rtbNameInput4, bidInput4, latencyInput4));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.auctionBidFloor = floorPrice;
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceValue);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), expectedWinnerBidValue);

    }

    @Test
    // 5. only 1 bid
    public void testOnlyOneBid() {

        Double bidFloorInput = .70d;
        Double bidInputVal1 = 1d;
        Long latencyInputVal1 = 100l;
        Double expectedSecondPriceVal = .70d;
        String expectedRTBAdNetworkName = "A";

        AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.RTBD.getValue());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.auctionBidFloor = bidFloorInput;
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceVal);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBAdNetworkName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), bidInputVal1);

    }

    @Test
    // 7. all lower than floor price
    public void testAllLowerThanFloorPrice() {

        Double bidFloorInput = .70d;
        Double bidInputVal1 = .50d;
        Long latencyInputVal1 = 100l;
        Double bidInputVal2 = .60d;
        Long latencyInputVal2 = 100l;
        Double bidInputVal3 = .40d;
        Long latencyInputVal3 = 150l;
        Double bidInputVal4 = .50d;
        Long latencyInputVal4 = 100l;
        Double expectedAuctionEngineResponse = null;

        AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.RTBD.getValue());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1));
        rtbSegments.add(setBidder("advId2", "channelId2", "externalSiteKey2", "B", bidInputVal2, latencyInputVal2));
        rtbSegments.add(setBidder("advId3", "channelId3", "externalSiteKey3", "C", bidInputVal3, latencyInputVal3));
        rtbSegments.add(setBidder("advId4", "channelId4", "externalSiteKey4", "D", bidInputVal4, latencyInputVal4));
        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.auctionBidFloor = bidFloorInput;
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        try {
            secondPrice1.getValue();
            Assert.fail("Value is not suppose to set.");
        }
        catch (AssertionError er) {

        }
        Assert.assertEquals(auctionEngineResponse, expectedAuctionEngineResponse);
    }

    @Test
    // 25. Only 1 bid with price 0.1 above floor price
    public void testOnlyOneBidWithPrice1AboveFloorPrice() {

        Double bidFloorInput = .70d;
        Double bidInputVal1 = .71d;
        Long latencyInputVal1 = 100l;
        Double expectedSecondPriceVal = .70d;
        String expectedRTBAdNetworkName = "A";

        AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.RTBD.getValue());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1));

        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.auctionBidFloor = bidFloorInput;
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        Assert.assertEquals(secondPrice1.getValue(), expectedSecondPriceVal);
        Assert.assertEquals(auctionEngineResponse.getName(), expectedRTBAdNetworkName);
        Assert.assertEquals(auctionEngineResponse.getBidPriceInUsd(), bidInputVal1);

    }

    @Test
    // 26. Only 1 bid with price 0.1 below floor price. this bid will get
    // directly cut off at floor price filter
    public void testOnlyOneBidWithPrice1BelowFloorPrice() {

        Double bidFloorInput = .70d;
        Double bidInputVal1 = .69d;
        Long latencyInputVal1 = 100l;
        String expectedAuctionEngineResponse = null;

        AuctionEngine auctionEngine = new AuctionEngine();
        auctionEngine.sasParams = new SASRequestParameters();
        auctionEngine.sasParams.setDst(DemandSourceType.RTBD.getValue());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

        rtbSegments.add(setBidder("advId1", "channelId1", "externalSiteKey1", "A", bidInputVal1, latencyInputVal1));

        auctionEngine.setUnfilteredChannelSegmentList(rtbSegments);

        casInternalRequestParameters.auctionBidFloor = bidFloorInput;
        auctionEngine.casInternalRequestParameters = casInternalRequestParameters;

        AdNetworkInterface auctionEngineResponse = auctionEngine.runAuctionEngine();

        try {
            secondPrice1.getValue();
            Assert.fail("Value is not suppose to set.");
        }
        catch (AssertionError er) {

        }

        Assert.assertEquals(auctionEngineResponse, expectedAuctionEngineResponse);

    }
}