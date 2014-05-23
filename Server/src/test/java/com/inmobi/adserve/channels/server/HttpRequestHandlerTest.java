package com.inmobi.adserve.channels.server;

import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.requesthandler.filters.TestScopeModule;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.easymock.classextension.EasyMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class HttpRequestHandlerTest extends TestCase {

    private static ChannelEntity channelentity;
    private static List<String> aDomains = new ArrayList<>();
    private static List<Integer> cAttributes = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        ChannelEntity.Builder builder = ChannelEntity.newBuilder();
        builder.setAccountId("advId");
        channelentity = builder.build();
        ConfigurationLoader config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
        CasConfigUtil.init(config, null);

        Configuration loggerConfig = createMock(Configuration.class);
        expect(loggerConfig.getString("advertiser")).andReturn("advertiser").anyTimes();
        expect(loggerConfig.getString("sampledadvertiser")).andReturn("sampledadvertiser").anyTimes();

        replay(loggerConfig);

        Configuration mockServerConfig = createMock(Configuration.class);
        expect(mockServerConfig.getInt("percentRollout", 100)).andReturn(100).anyTimes();
        expect(mockServerConfig.getList("allowedSiteTypes")).andReturn(null).anyTimes();
        expect(mockServerConfig.getBoolean("enableDatabusLogging")).andReturn(true).anyTimes();
        expect(mockServerConfig.getBoolean("enableFileLogging")).andReturn(true).anyTimes();
        expect(mockServerConfig.getInt("sampledadvertisercount")).andReturn(10).anyTimes();
        expect(mockServerConfig.getInt("maxconnections")).andReturn(100).anyTimes();
        replay(mockServerConfig);

        ConfigurationLoader mockConfigLoader = createMock(ConfigurationLoader.class);
        expect(mockConfigLoader.getLoggerConfiguration()).andReturn(loggerConfig).anyTimes();
        expect(mockConfigLoader.getAdapterConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getServerConfiguration()).andReturn(mockServerConfig).anyTimes();
        expect(mockConfigLoader.getRtbConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getLog4jConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getDatabaseConfiguration()).andReturn(null).anyTimes();
        replay(mockConfigLoader);

        RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.queryCreativeRepository(org.easymock.EasyMock.isA(String.class), org.easymock.EasyMock.isA(String.class))).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(null).anyTimes();
        replay(repositoryHelper);

        Guice.createInjector(Modules.override(
                new ServerModule(config, repositoryHelper),
                new CasNettyModule(config.getServerConfiguration())).with(new TestScopeModule()));

        AbstractMessagePublisher mockAbstractMessagePublisher = createMock(AbstractMessagePublisher.class);
        Logging.init(mockAbstractMessagePublisher, "cas-rr", "cas-advertisement", "null", mockServerConfig);
        aDomains.add("a.com");
        cAttributes.add(1);
    }

    @Test
    public void testrunRtbSecondPriceAuctionEngine() {
        ResponseSender httpRequestHandler = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface1);
        AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface2.getBidPriceInUsd()).andReturn((double) 4).anyTimes();
        expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
        expect(adNetworkInterface2.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface2.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface2.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface2.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface2.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface2.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface2.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        adNetworkInterface2.setSecondBidPrice(EasyMock.isA(Double.class));
        adNetworkInterface2.setEncryptedBid(EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        replay(adNetworkInterface2);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        ChannelSegment channelSegment2 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface2,
                0);
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment2);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.getAuctionEngine()
                .runRtbSecondPriceAuctionEngine();
        assertEquals(4, adNetworkInterfaceResult.getLatency());
    }

    @Test
    public void testrunRtbSecondPriceAuctionEngineTotalsegmentone() {
        ResponseSender httpRequestHandler = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        adNetworkInterface1.setSecondBidPrice(EasyMock.isA(Double.class));
        adNetworkInterface1.setEncryptedBid(EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        replay(adNetworkInterface1);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment1);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.getAuctionEngine()
                .runRtbSecondPriceAuctionEngine();
        assertEquals(2, adNetworkInterfaceResult.getLatency());
    }

    @Test
    public void testrunRtbSecondPriceAuctionEngineTotalsegmentZero() {
        ResponseSender httpRequestHandler = new ResponseSender();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.getAuctionEngine()
                .runRtbSecondPriceAuctionEngine();
        assertEquals(null, adNetworkInterfaceResult);
    }

    @Test
    public void testrunRtbSecondPriceAuctionEngineTopTwoEqualBid() {
        ResponseSender httpRequestHandler = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface1);
        AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface2.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
        expect(adNetworkInterface2.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface2.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface2.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface2.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface2.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface2.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface2.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface3.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
        expect(adNetworkInterface3.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface3.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface3.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface3.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface3.getCurrency()).andReturn("USD").anyTimes();
        adNetworkInterface3.setSecondBidPrice(EasyMock.isA(Double.class));
        adNetworkInterface3.setEncryptedBid(EasyMock.isA(String.class));
        expect(adNetworkInterface3.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface3.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        EasyMock.expectLastCall();
        replay(adNetworkInterface3);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        ChannelSegment channelSegment2 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface2,
                0);
        ChannelSegment channelSegment3 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface3,
                0);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment2);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment3);
        AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.getAuctionEngine()
                .runRtbSecondPriceAuctionEngine();
        assertEquals(1, adNetworkInterfaceResult.getLatency());
    }

    @Test
    public void testrunRtbSecondPriceAuctionEngineSecondhighestBidAtThirdPlace() {
        ResponseSender httpRequestHandler = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        adNetworkInterface1.setSecondBidPrice(EasyMock.isA(Double.class));
        adNetworkInterface1.setEncryptedBid(EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        replay(adNetworkInterface1);
        AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface2.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
        expect(adNetworkInterface2.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface2.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface2.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface2.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface2.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface2.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface2.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface3.getBidPriceInUsd()).andReturn((double) 1).anyTimes();
        expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
        expect(adNetworkInterface3.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface3.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface3.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface3.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface3.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface3.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface3.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface3);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        ChannelSegment channelSegment2 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface2,
                0);
        ChannelSegment channelSegment3 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface3,
                0);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment2);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment3);
        AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.getAuctionEngine()
                .runRtbSecondPriceAuctionEngine();
        assertEquals(2, adNetworkInterfaceResult.getLatency());
    }

    @Test
    public void testrunRtbSecondPriceAuctionEngineSecondHighestBidAtSecondPlace() {
        ResponseSender rs = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        adNetworkInterface1.setSecondBidPrice(EasyMock.isA(Double.class));
        adNetworkInterface1.setEncryptedBid(EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        replay(adNetworkInterface1);
        AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface2.getBidPriceInUsd()).andReturn((double) 1).anyTimes();
        expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
        expect(adNetworkInterface2.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface2.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface2.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface2.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface2.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface2.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface2.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface2.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface3.getBidPriceInUsd()).andReturn((double) 0).anyTimes();
        expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
        expect(adNetworkInterface3.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface3.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface3.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface3.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface3.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface3.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface3.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface3.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface3);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        ChannelSegment channelSegment2 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface2,
                0);
        ChannelSegment channelSegment3 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface3,
                0);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        rs.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        rs.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        rs.getAuctionEngine().getRtbSegments().add(channelSegment1);
        rs.getAuctionEngine().getRtbSegments().add(channelSegment2);
        rs.getAuctionEngine().getRtbSegments().add(channelSegment3);
        AdNetworkInterface adNetworkInterfaceResult = rs.getAuctionEngine().runRtbSecondPriceAuctionEngine();
        assertEquals(2, adNetworkInterfaceResult.getLatency());
    }

    @Test
    public void testisAllRtbCompleteTrue() {
        ResponseSender httpRequestHandler = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
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
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface1);
        AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
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
        adNetworkInterface2.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
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
        adNetworkInterface3.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface3);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        ChannelSegment channelSegment2 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface2,
                0);
        ChannelSegment channelSegment3 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface3,
                0);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment2);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment3);
        boolean result = httpRequestHandler.getAuctionEngine().isAllRtbComplete();
        assertEquals(true, result);
    }

    @Test
    public void testisAllRtbCompletefalse() {
        ResponseSender httpRequestHandler = new ResponseSender();
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
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
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface1);
        AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
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
        adNetworkInterface2.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface2.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface2.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface2.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface2);
        AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
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
        adNetworkInterface3.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface3.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface3.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface3.getIUrl()).andReturn("iurl").anyTimes();
        replay(adNetworkInterface3);
        ChannelSegment channelSegment1 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface1,
                0);
        ChannelSegment channelSegment2 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface2,
                0);
        ChannelSegment channelSegment3 = new ChannelSegment(null, channelentity, null, null, null, adNetworkInterface3,
                0);
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.rtbBidFloor = 0;
        casInternalRequestParameters.auctionId = "auctionId";
        casInternalRequestParameters.siteAccountType = AccountType.SELF_SERVE;
        httpRequestHandler.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
        httpRequestHandler.getAuctionEngine().setRtbSegments(new ArrayList<ChannelSegment>());
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment1);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment2);
        httpRequestHandler.getAuctionEngine().getRtbSegments().add(channelSegment3);
        boolean result = httpRequestHandler.getAuctionEngine().isAllRtbComplete();
        assertEquals(false, result);
    }

    @Test
    public void testWriteLogsBothListNull() {
        ResponseSender responseSender = new ResponseSender();
        responseSender.writeLogs();
    }

    @Test
    public void testWriteLogsRTBListNotNull() {
        AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
        expect(adNetworkInterface1.getBidPriceInUsd()).andReturn((double) 2).anyTimes();
        expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
        expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
        expect(adNetworkInterface1.getAdStatus()).andReturn("AD").anyTimes();
        expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
        expect(adNetworkInterface1.getId()).andReturn("SampleRTBId").anyTimes();
        expect(adNetworkInterface1.getHttpResponseContent()).andReturn("SampleRTBResponseContent").anyTimes();
        expect(adNetworkInterface1.getRequestUrl()).andReturn("SampleRTBURL").anyTimes();
        expect(adNetworkInterface1.getConnectionLatency()).andReturn(23l).anyTimes();
        expect(adNetworkInterface1.getAuctionId()).andReturn("auctionId").anyTimes();
        expect(adNetworkInterface1.getRtbImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getImpressionId()).andReturn("impressionId").anyTimes();
        expect(adNetworkInterface1.getSeatId()).andReturn("advId").anyTimes();
        expect(adNetworkInterface1.getCurrency()).andReturn("USD").anyTimes();
        expect(adNetworkInterface1.isRtbPartner()).andReturn(true).anyTimes();
        expect(adNetworkInterface1.isLogCreative()).andReturn(false).anyTimes();
        expect(adNetworkInterface1.getCreativeId()).andReturn("creativeId").anyTimes();
        adNetworkInterface1.setLogCreative(true);
        org.easymock.EasyMock.expectLastCall().anyTimes();
        expect(adNetworkInterface1.getADomain()).andReturn(aDomains).anyTimes();
        expect(adNetworkInterface1.getAttribute()).andReturn(cAttributes).anyTimes();
        expect(adNetworkInterface1.getIUrl()).andReturn("iurl").anyTimes();
        ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.adStatus = "AD";
        thirdPartyAdResponse.latency = 12;
        expect(adNetworkInterface1.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        replay(adNetworkInterface1);
        ChannelSegmentEntity channelSegmentEntity = createMock(ChannelSegmentEntity.class);
        expect(channelSegmentEntity.getExternalSiteKey()).andReturn("ext").anyTimes();
        expect(channelSegmentEntity.getAdvertiserId()).andReturn("extId").anyTimes();
        expect(channelSegmentEntity.getCampaignIncId()).andReturn(1l).anyTimes();
        expect(channelSegmentEntity.getAdgroupIncId()).andReturn(1l).anyTimes();
        expect(channelSegmentEntity.getDst()).andReturn(2).anyTimes();
        replay(channelSegmentEntity);
        ChannelSegment channelSegment1 = new ChannelSegment(channelSegmentEntity, channelentity, null, null, null,
                adNetworkInterface1, 0);
        List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        list.add(channelSegment1);
        ResponseSender responseSender = new ResponseSender();
        responseSender.getAuctionEngine().setRtbSegments(list);
        responseSender.writeLogs();
    }

}
