package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import org.junit.Test;

import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.casthrift.DemandSourceType;


public class HttpRequestHandlerTest {

    private static List<String> aDomains = new ArrayList<>();
    private static List<Integer> cAttributes = new ArrayList<>();

    @Test
    public void testisAllRtbCompleteTrue() {
        final ResponseSender httpRequestHandler = new ResponseSender();
        final AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface1.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface1.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface1);
        final AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface2.getBidPriceInUsd()).andReturn((double) 1).anyTimes();
        expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
        expect(adNetworkInterface2.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface2.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface2.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface2.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface2.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface2.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface2.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface2.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        final AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface3.getBidPriceInUsd()).andReturn((double) 0).anyTimes();
        expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
        expect(adNetworkInterface3.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface3.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface3.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface3.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface3.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface3.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface3.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface3.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface3);
        final ChannelSegment channelSegment1 =
                new ChannelSegment(null, null, null, null, null, adNetworkInterface1, 0);
        final ChannelSegment channelSegment2 =
                new ChannelSegment(null, null, null, null, null, adNetworkInterface2, 0);
        final ChannelSegment channelSegment3 =
                new ChannelSegment(null, null, null, null, null, adNetworkInterface3, 0);
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setAuctionBidFloor(0);
        casInternalRequestParameters.setAuctionId("auctionId");
        casInternalRequestParameters.setSiteAccountType(AccountType.SELF_SERVE);
        httpRequestHandler.getAuctionEngine().casParams = casInternalRequestParameters;
        httpRequestHandler.getAuctionEngine().sasParams = new SASRequestParameters();
        httpRequestHandler.getAuctionEngine().sasParams.setDst(DemandSourceType.RTBD.getValue());
        httpRequestHandler.getAuctionEngine().setUnfilteredChannelSegmentList(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getUnfilteredChannelSegmentList().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getUnfilteredChannelSegmentList().add(channelSegment2);
        httpRequestHandler.getAuctionEngine().getUnfilteredChannelSegmentList().add(channelSegment3);
        final boolean result = httpRequestHandler.getAuctionEngine().areAllChannelSegmentRequestsComplete();
        assertEquals(true, result);
    }

    @Test
    public void testisAllRtbCompletefalse() {
        final ResponseSender httpRequestHandler = new ResponseSender();
        final AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface1.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface1.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface1);
        final AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface2.getBidPriceInUsd()).andReturn((double) 1).anyTimes();
        expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
        expect(adNetworkInterface2.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface2.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface2.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface2.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface2.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface2.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface2.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface2.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface2.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        final AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface3.getBidPriceInUsd()).andReturn((double) 0).anyTimes();
        expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
        expect(adNetworkInterface3.isRequestCompleted()).andReturn(false).anyTimes();
        expect(adNetworkInterface3.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface3.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface3.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface3.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface3.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface3.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface3.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface3.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface3);
        final ChannelSegment channelSegment1 =
                new ChannelSegment(null, null, null, null, null, adNetworkInterface1, 0);
        final ChannelSegment channelSegment2 =
                new ChannelSegment(null, null, null, null, null, adNetworkInterface2, 0);
        final ChannelSegment channelSegment3 =
                new ChannelSegment(null, null, null, null, null, adNetworkInterface3, 0);
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setAuctionBidFloor(0);
        casInternalRequestParameters.setAuctionId("auctionId");
        casInternalRequestParameters.setSiteAccountType(AccountType.SELF_SERVE);
        httpRequestHandler.getAuctionEngine().casParams = casInternalRequestParameters;
        httpRequestHandler.getAuctionEngine().sasParams = new SASRequestParameters();
        httpRequestHandler.getAuctionEngine().sasParams.setDst(DemandSourceType.RTBD.getValue());
        httpRequestHandler.getAuctionEngine().setUnfilteredChannelSegmentList(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getUnfilteredChannelSegmentList().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getUnfilteredChannelSegmentList().add(channelSegment2);
        httpRequestHandler.getAuctionEngine().getUnfilteredChannelSegmentList().add(channelSegment3);
        final boolean result = httpRequestHandler.getAuctionEngine().areAllChannelSegmentRequestsComplete();
        assertEquals(false, result);
    }

    @Test
    public void testWriteLogsBothListNull() {
        final ResponseSender responseSender = new ResponseSender();
        responseSender.writeLogs();
    }

    @Test
    public void testWriteLogsRTBListNotNull() {
        final AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getId()).andReturn("SampleRTBId").anyTimes();
        expect(adNetworkInterface1.getHttpResponseContent()).andReturn("SampleRTBResponseContent").anyTimes();
        expect(adNetworkInterface1.getRequestUrl()).andReturn("SampleRTBURL").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.isRtbPartner()).andReturn(true).anyTimes();
        expect(adNetworkInterface1.isLogCreative()).andReturn(false).anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(adNetworkInterface1.getDst()).andReturn(DemandSourceType.RTBD).anyTimes();
        adNetworkInterface1.setLogCreative(true);

        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        final ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.setAdStatus("AD");
        thirdPartyAdResponse.setLatency(12);
        expect(adNetworkInterface1.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        replay(adNetworkInterface1);
        final ChannelSegmentEntity channelSegmentEntity = createMock(ChannelSegmentEntity.class);
        expect(channelSegmentEntity.getExternalSiteKey()).andReturn("ext").anyTimes();
        expect(channelSegmentEntity.getAdvertiserId()).andReturn("extId").anyTimes();
        expect(channelSegmentEntity.getCampaignIncId()).andReturn(1l).anyTimes();
        expect(channelSegmentEntity.getAdgroupIncId()).andReturn(1l).anyTimes();
        expect(channelSegmentEntity.getDst()).andReturn(2).anyTimes();
        replay(channelSegmentEntity);
        final ChannelSegment channelSegment1 =
                new ChannelSegment(channelSegmentEntity, null, null, null, null, adNetworkInterface1, 0);
        final List<ChannelSegment> list = new ArrayList<>();
        list.add(channelSegment1);
        final ResponseSender responseSender = new ResponseSender();
        responseSender.getAuctionEngine().setUnfilteredChannelSegmentList(list);
        responseSender.writeLogs();
    }

}
