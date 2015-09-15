package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.AuctionType;
import com.inmobi.adserve.adpool.Creative;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mvp.HostedAdNetwork;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.types.AdIdChain;
import com.inmobi.types.GUID;
import com.inmobi.types.PricingModel;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, Logging.class})
public class ResponseSenderTest {

    @BeforeClass
    public static void setUp() {
        final short hostIdCode = (short) 5;
        final byte dataCenterIdCode = 1;
        ImpressionIdGenerator.init(hostIdCode, dataCenterIdCode);
    }

    @Test
    public void testGetResponseFormat() throws Exception {
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockSASRequestParameters.getRFormat()).andReturn(null).times(2).andReturn("axml").times(1)
                .andReturn("xhtml").times(1).andReturn("html").times(1).andReturn("imai").times(1).andReturn("native")
                .times(1).andReturn("jsAdCode").times(1);

        expect(mockSASRequestParameters.getAdcode()).andReturn("JS").times(1).andReturn(null).anyTimes();

        expect(mockSASRequestParameters.getRqIframe()).andReturn("<Iframe Code>").times(1).andReturn(null).anyTimes();

        replayAll();

        ResponseSender responseSender = new ResponseSender();

        responseSender.setSasParams(null);
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.HTML)));

        responseSender.setSasParams(mockSASRequestParameters);
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.JS_AD_CODE)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.HTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.XHTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.XHTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.HTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.IMAI)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.NATIVE)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseSender.ResponseFormat.JS_AD_CODE)));
    }

    @Test
    public void testWriteLogsFailure() throws Exception {
        mockStaticNice(Logging.class);
        mockStaticNice(InspectorStats.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);

        List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        expect(mockAuctionEngine.getUnfilteredChannelSegmentList()).andReturn(list).times(2).andReturn(null).times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.DCP.getValue()).times(1)
                .andReturn(DemandSourceType.IX.getValue()).times(1);
        replayAll();

        ResponseSender responseSender = new ResponseSender();
        responseSender.setSasParams(null);
        responseSender.writeLogs();

        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        list.add(null);
        responseSender.setRankList(list);
        responseSender.writeLogs();
        responseSender.setRankList(null);
        responseSender.writeLogs();
        responseSender.setRankList(list);
        responseSender.writeLogs();
    }

    @Test
    public void testWriteLogsSuccess() throws Exception {
        mockStaticNice(Logging.class);
        mockStaticNice(InspectorStats.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);

        expect(mockAuctionEngine.getUnfilteredChannelSegmentList()).andReturn(new ArrayList<ChannelSegment>()).times(1)
                .andReturn(null).times(1);
        expect(mockAuctionEngine.getAuctionResponse()).andReturn(getDummyChannelSegment()).times(2).andReturn(null)
                .times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).times(3)
                .andReturn(DemandSourceType.DCP.getValue()).times(3);
        replayAll();

        ResponseSender responseSender = new ResponseSender();
        responseSender.casInternalRequestParameters = new CasInternalRequestParameters();
        responseSender.casInternalRequestParameters.setAuctionBidFloor(1.5);

        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        responseSender.setRankList(null);
        responseSender.writeLogs();
        responseSender.setRankList(Arrays.asList(getDummyChannelSegment()));
        responseSender.writeLogs();

    }

    @Test
    public void testCreateThriftResponseRTBDCurrencyIsUSD() throws Exception {
        String adMarkup = "<AD Content>";
        String adGroupId = "adGroupId";
        String adId = "adId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String impressionId = TestUtils.SampleStrings.impressionId;
        String currency = "USD";
        long incId = 123L;
        long adGroupIncId = 456L;
        long campaignIncId = 789L;
        Double bidPriceInUSD = 5.5;
        Double secondBidPriceInUSD = 4.5;
        short selectedSlot = 5;
        ADCreativeType creativeType = ADCreativeType.BANNER;
        DemandSourceType dst = DemandSourceType.RTBD;

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        RtbAdNetwork mockRtbAdNetwork = createMock(RtbAdNetwork.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);

        expect(mockAuctionEngine.getAuctionResponse()).andReturn(mockChannelSegment).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockRtbAdNetwork).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adGroupId).anyTimes();
        expect(mockChannelSegmentEntity.getAdId(creativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(creativeType)).andReturn(incId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adGroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockRtbAdNetwork.getDst()).andReturn(dst).anyTimes();
        expect(mockRtbAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(mockRtbAdNetwork.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockRtbAdNetwork.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockRtbAdNetwork.getSelectedSlotId()).andReturn(selectedSlot).anyTimes();
        expect(mockRtbAdNetwork.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUSD).anyTimes();
        expect(mockRtbAdNetwork.getCurrency()).andReturn(currency).anyTimes();
        expect(mockRtbAdNetwork.getRepositoryHelper()).andReturn(mockRepositoryHelper).anyTimes();

        replayAll();

        ResponseSender responseSender = new ResponseSender();
        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        long bid = (long) (bidPriceInUSD * Math.pow(10, 6));
        long minBid = (long) (secondBidPriceInUSD * Math.pow(10, 6));
        UUID uuid = UUID.fromString(impressionId);
        GUID impression = new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());

        AdPoolResponse adPoolResponse =
                responseSender.createThriftResponse(adMarkup, mockRtbAdNetwork.getRepositoryHelper());
        AdInfo adInfo = adPoolResponse.getAds().get(0);
        Creative creative = adInfo.creative;
        AdIdChain adIdChain = adInfo.deprecatedAdIds.get(0);

        assertThat(adIdChain.adgroup_guid, is(equalTo(adGroupId)));
        assertThat(adIdChain.ad_guid, is(equalTo(adId)));
        assertThat(adIdChain.campaign_guid, is(equalTo(campaignId)));
        assertThat(adIdChain.ad, is(equalTo(incId)));
        assertThat(adIdChain.group, is(equalTo(adGroupIncId)));
        assertThat(adIdChain.campaign, is(equalTo(campaignIncId)));
        assertThat(adIdChain.advertiser_guid, is(equalTo(advertiserId)));
        assertThat(adInfo.pricingModel, is(equalTo(PricingModel.CPM)));
        assertThat(adInfo.auctionType, is(equalTo(AuctionType.SECOND_PRICE)));
        assertThat(adInfo.bid, is(equalTo(bid)));
        assertThat(adInfo.price, is(equalTo(bid)));
        assertThat(adInfo.deprecatedImpressionId, is(equalTo(impression)));
        assertThat(creative.getValue(), is(equalTo(adMarkup)));
        assertThat(adPoolResponse.minChargedValue, is(equalTo(minBid)));
    }

    @Test
    public void testCreateThriftResponseDCPCurrencyIsINR() throws Exception {
        String adMarkup = "<AD Content>";
        String adGroupId = "adGroupId";
        String adId = "adId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String impressionId = TestUtils.SampleStrings.impressionId;
        String currency = "INR";
        long incId = 123L;
        long adGroupIncId = 456L;
        long campaignIncId = 789L;
        Double bidPriceInUSD = 5.5;
        Double secondBidPriceInUSD = 4.5;
        Double bidPriceInLocal = 275.0;
        short selectedSlot = 5;
        ADCreativeType creativeType = ADCreativeType.BANNER;
        DemandSourceType dst = DemandSourceType.DCP;

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        AdNetworkInterface mockDCPAdNetwork = createMock(AdNetworkInterface.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);

        expect(mockAuctionEngine.getAuctionResponse()).andReturn(mockChannelSegment).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockDCPAdNetwork).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adGroupId).anyTimes();
        expect(mockChannelSegmentEntity.getAdId(creativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(creativeType)).andReturn(incId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adGroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockDCPAdNetwork.getDst()).andReturn(dst).anyTimes();
        expect(mockDCPAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(mockDCPAdNetwork.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockDCPAdNetwork.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockDCPAdNetwork.getSelectedSlotId()).andReturn(selectedSlot).anyTimes();
        expect(mockDCPAdNetwork.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUSD).anyTimes();
        expect(mockDCPAdNetwork.getBidPriceInLocal()).andReturn(bidPriceInLocal).anyTimes();
        expect(mockDCPAdNetwork.getCurrency()).andReturn(currency).anyTimes();
        expect(mockDCPAdNetwork.getRepositoryHelper()).andReturn(mockRepositoryHelper).anyTimes();
        replayAll();

        ResponseSender responseSender = new ResponseSender();
        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        long bid = (long) (bidPriceInUSD * Math.pow(10, 6));
        long localBid = (long) (bidPriceInLocal * Math.pow(10, 6));
        long minBid = (long) (secondBidPriceInUSD * Math.pow(10, 6));
        UUID uuid = UUID.fromString(impressionId);
        GUID impression = new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());

        AdPoolResponse adPoolResponse =
                responseSender.createThriftResponse(adMarkup, mockDCPAdNetwork.getRepositoryHelper());
        AdInfo adInfo = adPoolResponse.getAds().get(0);
        Creative creative = adInfo.creative;
        AdIdChain adIdChain = adInfo.deprecatedAdIds.get(0);

        assertThat(adIdChain.adgroup_guid, is(equalTo(adGroupId)));
        assertThat(adIdChain.ad_guid, is(equalTo(adId)));
        assertThat(adIdChain.campaign_guid, is(equalTo(campaignId)));
        assertThat(adIdChain.ad, is(equalTo(incId)));
        assertThat(adIdChain.group, is(equalTo(adGroupIncId)));
        assertThat(adIdChain.campaign, is(equalTo(campaignIncId)));
        assertThat(adIdChain.advertiser_guid, is(equalTo(advertiserId)));
        assertThat(adInfo.pricingModel, is(equalTo(PricingModel.CPM)));
        assertThat(adInfo.auctionType, is(equalTo(AuctionType.SECOND_PRICE)));
        assertThat(adInfo.bid, is(equalTo(bid)));
        assertThat(adInfo.price, is(equalTo(bid)));
        assertThat(adInfo.deprecatedImpressionId, is(equalTo(impression)));
        assertThat(adInfo.originalCurrencyName, is(equalTo(currency)));
        assertThat(adInfo.bidInOriginalCurrency, is(equalTo(localBid)));
        assertThat(creative.getValue(), is(equalTo(adMarkup)));
        assertThat(adPoolResponse.minChargedValue, is(equalTo(minBid)));
    }

    @Test
    public void testCreateThriftResponseHostedAdServer() throws Exception {
        String adMarkup = "<AD Content>";
        String adGroupId = "adGroupId";
        String adId = "adId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String impressionId = TestUtils.SampleStrings.impressionId;
        String currency = "USD";
        long incId = 123L;
        long adGroupIncId = 456L;
        long campaignIncId = 789L;
        Double bidPriceInUSD = 5.5;
        Double secondBidPriceInUSD = 4.5;
        short selectedSlot = 5;
        ADCreativeType creativeType = ADCreativeType.BANNER;
        DemandSourceType dst = DemandSourceType.RTBD;

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        HostedAdNetwork mockHostedAdNetwork = createMock(HostedAdNetwork.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);

        expect(mockAuctionEngine.getAuctionResponse()).andReturn(mockChannelSegment).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockHostedAdNetwork).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adGroupId).anyTimes();
        expect(mockChannelSegmentEntity.getAdId(creativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(creativeType)).andReturn(incId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adGroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockHostedAdNetwork.getDst()).andReturn(dst).anyTimes();
        expect(mockHostedAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(mockHostedAdNetwork.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockHostedAdNetwork.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockHostedAdNetwork.getSelectedSlotId()).andReturn(selectedSlot).anyTimes();
        expect(mockHostedAdNetwork.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUSD).anyTimes();
        expect(mockHostedAdNetwork.getCurrency()).andReturn(currency).anyTimes();
        expect(mockHostedAdNetwork.getRepositoryHelper()).andReturn(mockRepositoryHelper).anyTimes();

        replayAll();

        ResponseSender responseSender = new ResponseSender();
        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        long bid = (long) (bidPriceInUSD * Math.pow(10, 6));
        long minBid = (long) (secondBidPriceInUSD * Math.pow(10, 6));
        UUID uuid = UUID.fromString(impressionId);
        GUID impression = new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());

        AdPoolResponse adPoolResponse =
                responseSender.createThriftResponse(adMarkup, mockHostedAdNetwork.getRepositoryHelper());
        AdInfo adInfo = adPoolResponse.getAds().get(0);
        Creative creative = adInfo.creative;
        AdIdChain adIdChain = adInfo.deprecatedAdIds.get(0);

        assertThat(adIdChain.adgroup_guid, is(equalTo(adGroupId)));
        assertThat(adIdChain.ad_guid, is(equalTo(adId)));
        assertThat(adIdChain.campaign_guid, is(equalTo(campaignId)));
        assertThat(adIdChain.ad, is(equalTo(incId)));
        assertThat(adIdChain.group, is(equalTo(adGroupIncId)));
        assertThat(adIdChain.campaign, is(equalTo(campaignIncId)));
        assertThat(adIdChain.advertiser_guid, is(equalTo(advertiserId)));
        assertThat(adInfo.pricingModel, is(equalTo(PricingModel.CPC)));
        assertThat(adInfo.auctionType, is(equalTo(AuctionType.TRUMP)));
        assertThat(adInfo.bid, is(equalTo(bid)));
        assertThat(adInfo.price, is(equalTo(bid)));
        assertThat(adInfo.deprecatedImpressionId, is(equalTo(impression)));
        assertThat(creative.getValue(), is(equalTo(adMarkup)));
        assertThat(adPoolResponse.minChargedValue, is(equalTo(minBid)));
    }

    @Test
    public void testCreateThriftResponseIX() throws Exception {
        String adMarkup = "<AD Content>";
        String adGroupId = "adGroupId";
        String adId = "adId";
        String campaignId = "campaignId";
        String advertiserId = "advertiserId";
        String impressionId = TestUtils.SampleStrings.impressionId;
        String currency = "USD";
        String dealId = "dealId";
        long incId = 123L;
        long adGroupIncId = 456L;
        long campaignIncId = 789L;
        Double bidPriceInUSD = 5.5;
        Double secondBidPriceInUSD = 4.5;
        Double adjustBidPrice = 3.5;
        short selectedSlot = 5;
        ADCreativeType creativeType = ADCreativeType.BANNER;
        DemandSourceType dst = DemandSourceType.IX;
        IXPackageEntity.Builder builder = IXPackageEntity.newBuilder();
        List<String> accessTypes = new ArrayList<String>(); 
        accessTypes.add(ResponseSender.RIGHT_TO_FIRST_REFUSAL_DEAL);
        builder.accessTypes(accessTypes);
        List<String> dealIds = new ArrayList<String>();
        dealIds.add("dealId");
        builder.dealIds(dealIds);
        IXPackageEntity ixPackageEntity = builder.build();

        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        IXAdNetwork mockIXAdNetwork = createMock(IXAdNetwork.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        mockStaticNice(InspectorStats.class);
        expect(mockRepositoryHelper.queryIxPackageByDeal("dealId")).andReturn(ixPackageEntity).anyTimes();

        expect(mockAuctionEngine.getAuctionResponse()).andReturn(mockChannelSegment).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegment.getAdNetworkInterface()).andReturn(mockIXAdNetwork).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn(adGroupId).anyTimes();
        expect(mockChannelSegmentEntity.getAdId(creativeType)).andReturn(adId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignId()).andReturn(campaignId).anyTimes();
        expect(mockChannelSegmentEntity.getIncId(creativeType)).andReturn(incId).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adGroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getCampaignIncId()).andReturn(campaignIncId).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockIXAdNetwork.getDst()).andReturn(dst).anyTimes();
        expect(mockIXAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(mockIXAdNetwork.getBidPriceInUsd()).andReturn(bidPriceInUSD).anyTimes();
        expect(mockIXAdNetwork.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockIXAdNetwork.getSelectedSlotId()).andReturn(selectedSlot).anyTimes();
        expect(mockIXAdNetwork.getSecondBidPriceInUsd()).andReturn(secondBidPriceInUSD).anyTimes();
        expect(mockIXAdNetwork.getCurrency()).andReturn(currency).anyTimes();
        expect(mockIXAdNetwork.getAdjustbid()).andReturn(adjustBidPrice).anyTimes();
        expect(mockIXAdNetwork.getRepositoryHelper()).andReturn(mockRepositoryHelper).anyTimes();

        expect(mockIXAdNetwork.isExternalPersonaDeal()).andReturn(false).times(2).andReturn(true).anyTimes();
        expect(mockIXAdNetwork.getDealId()).andReturn(null).times(1).andReturn(dealId).anyTimes();
        HashSet<Integer> temp = new HashSet<Integer>();
        temp.add(1);
        expect(mockIXAdNetwork.getUsedCsIds()).andReturn(temp).times(1).andReturn(null).anyTimes();
        replayAll();

        ResponseSender responseSender = new ResponseSender();
        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        long bid = (long) (bidPriceInUSD * Math.pow(10, 6));
        long minBid = (long) (secondBidPriceInUSD * Math.pow(10, 6));
        long adjustBid = (long) (adjustBidPrice * Math.pow(10, 6));
        UUID uuid = UUID.fromString(impressionId);
        GUID impression = new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());

        AdPoolResponse adPoolResponse =
                responseSender.createThriftResponse(adMarkup, mockIXAdNetwork.getRepositoryHelper());
        AdInfo adInfo = adPoolResponse.getAds().get(0);
        Creative creative = adInfo.creative;
        AdIdChain adIdChain = adInfo.deprecatedAdIds.get(0);

        assertThat(adIdChain.adgroup_guid, is(equalTo(adGroupId)));
        assertThat(adIdChain.ad_guid, is(equalTo(adId)));
        assertThat(adIdChain.campaign_guid, is(equalTo(campaignId)));
        assertThat(adIdChain.ad, is(equalTo(incId)));
        assertThat(adIdChain.group, is(equalTo(adGroupIncId)));
        assertThat(adIdChain.campaign, is(equalTo(campaignIncId)));
        assertThat(adIdChain.advertiser_guid, is(equalTo(advertiserId)));
        assertThat(adInfo.pricingModel, is(equalTo(PricingModel.CPM)));
        assertThat(adInfo.bid, is(equalTo(bid)));
        assertThat(adInfo.price, is(equalTo(bid)));
        assertThat(adInfo.deprecatedImpressionId, is(equalTo(impression)));
        assertThat(creative.getValue(), is(equalTo(adMarkup)));
        assertThat(adPoolResponse.minChargedValue, is(equalTo(minBid)));
        assertThat(adInfo.auctionType, is(equalTo(AuctionType.FIRST_PRICE)));


        adPoolResponse = responseSender.createThriftResponse(adMarkup, mockIXAdNetwork.getRepositoryHelper());
        adInfo = adPoolResponse.getAds().get(0);

        assertThat(adInfo.auctionType, is(equalTo(AuctionType.TRUMP)));
        assertThat(adInfo.dealId, is(equalTo(dealId)));
        assertThat(adInfo.highestBid, is(equalTo(adjustBid)));

        adPoolResponse = responseSender.createThriftResponse(adMarkup, mockIXAdNetwork.getRepositoryHelper());
        adInfo = adPoolResponse.getAds().get(0);

        assertThat(adInfo.auctionType, is(equalTo(AuctionType.TRUMP)));
        assertThat(adInfo.dealId, is(equalTo(dealId)));
        assertThat(adInfo.highestBid, is(equalTo(adjustBid)));

        adPoolResponse = responseSender.createThriftResponse(adMarkup, mockIXAdNetwork.getRepositoryHelper());
        adInfo = adPoolResponse.getAds().get(0);

        assertThat(adInfo.auctionType, is(equalTo(AuctionType.TRUMP)));
        assertThat(adInfo.dealId, is(equalTo(dealId)));
        assertThat(adInfo.highestBid, is(equalTo(adjustBid)));
        assertThat(adPoolResponse.isSetRequestPoolSpecificInfo(), is(equalTo(true)));

        adPoolResponse = responseSender.createThriftResponse(adMarkup, mockIXAdNetwork.getRepositoryHelper());
        assertThat(adPoolResponse.isSetRequestPoolSpecificInfo(), is(equalTo(false)));
    }

    private ChannelSegment getDummyChannelSegment() {
        return new ChannelSegment(null, null, null, null, null, null, 0.0);
    }

}
