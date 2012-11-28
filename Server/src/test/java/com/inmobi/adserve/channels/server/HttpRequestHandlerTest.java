package com.inmobi.adserve.channels.server;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.server.HttpRequestHandler.ChannelSegment;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;

import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.EasyMock.expect;

public class HttpRequestHandlerTest extends TestCase{
  
  private static ConfigurationLoader config;
  private static String rrFile = "";
  private static String channelFile = "";
  private static String debugFile  = "";
  private static int count = 0;
  
  public void setUp() throws Exception {
    Configuration loggerConfig = createMock(Configuration.class);
    expect(loggerConfig.getString("channel")).andReturn("channel").anyTimes();
    expect(loggerConfig.getString("rr")).andReturn("rr").anyTimes();
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
    expect(mockConfigLoader.loggerConfiguration()).andReturn(loggerConfig).anyTimes();
    expect(mockConfigLoader.adapterConfiguration()).andReturn(null).anyTimes();
    expect(mockConfigLoader.serverConfiguration()).andReturn(mockServerConfig).anyTimes();
    expect(mockConfigLoader.rtbConfiguration()).andReturn(null).anyTimes();
    expect(mockConfigLoader.log4jConfiguration()).andReturn(null).anyTimes();
    expect(mockConfigLoader.databaseConfiguration()).andReturn(null).anyTimes();
    replay(mockConfigLoader);
    
    Configuration mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/tmp/Channel-server.properties").anyTimes();
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    InspectorStats.initializeWorkflow("WorkFlow");
    HttpRequestHandler.init(mockConfigLoader, (ChannelAdGroupRepository) null, (InspectorStats) null, (ClientBootstrap) null, (ClientBootstrap) null, null, null, null,
        null);
    AbstractMessagePublisher mockAbstractMessagePublisher = createMock(AbstractMessagePublisher.class);
    Logging.init(mockAbstractMessagePublisher, "cas-rr", "cas-channel", "cas-advertisement", mockServerConfig);
  }
  
  @Test
  public void testrunRtbSecondPriceAuctionEngine() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  replay(adNetworkInterface1);
  AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface2.getBidprice()).andReturn((double) 4).anyTimes();
  expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
  replay(adNetworkInterface2);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  ChannelSegment channelSegment2 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface2, null, null);
  httpRequestHandler.rtbSegments.add(channelSegment1);
  httpRequestHandler.rtbSegments.add(channelSegment2);
  AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.runRtbSecondPriceAuctionEngine();
  assertEquals(4, adNetworkInterfaceResult.getLatency());
  }
  @Test
  public void testrunRtbSecondPriceAuctionEngineTotalsegmentone() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  replay(adNetworkInterface1);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  httpRequestHandler.rtbSegments.add(channelSegment1);
  AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.runRtbSecondPriceAuctionEngine();
  assertEquals(2, adNetworkInterfaceResult.getLatency());
  }
  @Test
  public void testrunRtbSecondPriceAuctionEngineTotalsegmentZero() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.runRtbSecondPriceAuctionEngine();
  assertEquals(null, adNetworkInterfaceResult);
  }
  @Test
  public void testrunRtbSecondPriceAuctionEngineTopTwoEqualBid() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  replay(adNetworkInterface1);
  AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface2.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
  replay(adNetworkInterface2);
  AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface3.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
  replay(adNetworkInterface3);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  ChannelSegment channelSegment2 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface2, null, null);
  ChannelSegment channelSegment3 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface3, null, null);
  httpRequestHandler.rtbSegments.add(channelSegment1);
  httpRequestHandler.rtbSegments.add(channelSegment2);
  httpRequestHandler.rtbSegments.add(channelSegment3);
  AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.runRtbSecondPriceAuctionEngine();
  assertEquals(1, adNetworkInterfaceResult.getLatency());
  }
  @Test
  public void testrunRtbSecondPriceAuctionEngineSecondhighestBidAtThirdPlace() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  replay(adNetworkInterface1);
  AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface2.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
  replay(adNetworkInterface2);
  AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface3.getBidprice()).andReturn((double) 1).anyTimes();
  expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
  replay(adNetworkInterface3);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  ChannelSegment channelSegment2 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface2, null, null);
  ChannelSegment channelSegment3 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface3, null, null);
  httpRequestHandler.rtbSegments.add(channelSegment1);
  httpRequestHandler.rtbSegments.add(channelSegment2);
  httpRequestHandler.rtbSegments.add(channelSegment3);
  AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.runRtbSecondPriceAuctionEngine();
  assertEquals(2, adNetworkInterfaceResult.getLatency());
  }
  @Test
  public void testrunRtbSecondPriceAuctionEngineSecondHighestBidAtSecondPlace() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  replay(adNetworkInterface1);
  AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface2.getBidprice()).andReturn((double) 1).anyTimes();
  expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
  replay(adNetworkInterface2);
  AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface3.getBidprice()).andReturn((double) 0).anyTimes();
  expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
  replay(adNetworkInterface3);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  ChannelSegment channelSegment2 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface2, null, null);
  ChannelSegment channelSegment3 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface3, null, null);
  httpRequestHandler.rtbSegments.add(channelSegment1);
  httpRequestHandler.rtbSegments.add(channelSegment2);
  httpRequestHandler.rtbSegments.add(channelSegment3);
  AdNetworkInterface adNetworkInterfaceResult = httpRequestHandler.runRtbSecondPriceAuctionEngine();
  assertEquals(2, adNetworkInterfaceResult.getLatency());
  }
  @Test
  public void testisAllRtbCompleteTrue() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
  replay(adNetworkInterface1);
  AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface2.getBidprice()).andReturn((double) 1).anyTimes();
  expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
  expect(adNetworkInterface2.isRequestCompleted()).andReturn(true).anyTimes();
  replay(adNetworkInterface2);
  AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface3.getBidprice()).andReturn((double) 0).anyTimes();
  expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
  expect(adNetworkInterface3.isRequestCompleted()).andReturn(true).anyTimes();
  replay(adNetworkInterface3);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  ChannelSegment channelSegment2 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface2, null, null);
  ChannelSegment channelSegment3 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface3, null, null);
  httpRequestHandler.rtbSegments.add(channelSegment1);
  httpRequestHandler.rtbSegments.add(channelSegment2);
  httpRequestHandler.rtbSegments.add(channelSegment3);
  boolean result = httpRequestHandler.isAllRtbComplete();
  assertEquals(true, result);
  }
  @Test
  public void testisAllRtbCompletefalse() {
  HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
  AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
  expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
  expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
  replay(adNetworkInterface1);
  AdNetworkInterface adNetworkInterface2 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface2.getBidprice()).andReturn((double) 1).anyTimes();
  expect(adNetworkInterface2.getLatency()).andReturn((long) 4).anyTimes();
  expect(adNetworkInterface2.isRequestCompleted()).andReturn(true).anyTimes();
  replay(adNetworkInterface2);
  AdNetworkInterface adNetworkInterface3 = createMock(RtbAdNetwork.class);
  expect(adNetworkInterface3.getBidprice()).andReturn((double) 0).anyTimes();
  expect(adNetworkInterface3.getLatency()).andReturn((long) 1).anyTimes();
  expect(adNetworkInterface3.isRequestCompleted()).andReturn(false).anyTimes();
  replay(adNetworkInterface3);
  ChannelSegment channelSegment1 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface1, null, null);  
  ChannelSegment channelSegment2 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface2, null, null);
  ChannelSegment channelSegment3 = httpRequestHandler.new ChannelSegment(null, adNetworkInterface3, null, null);
  httpRequestHandler.rtbSegments.add(channelSegment1);
  httpRequestHandler.rtbSegments.add(channelSegment2);
  httpRequestHandler.rtbSegments.add(channelSegment3);
  boolean result = httpRequestHandler.isAllRtbComplete();
  assertEquals(false, result);
  }
  
  
  @Test
  public void testWriteLogsBothListNull() {
    HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
    httpRequestHandler.writeLogs();
  }
  
  @Test
  public void testWriteLogsRTBListNotNull() {
    HttpRequestHandler httpRequestHandlerbase = new HttpRequestHandler();
    AdNetworkInterface adNetworkInterface1 = createMock(RtbAdNetwork.class);
    expect(adNetworkInterface1.getBidprice()).andReturn((double) 2).anyTimes();
    expect(adNetworkInterface1.getLatency()).andReturn((long) 2).anyTimes();
    expect(adNetworkInterface1.isRequestCompleted()).andReturn(true).anyTimes();
    expect(adNetworkInterface1.getName()).andReturn("SampleRTB").anyTimes();
    expect(adNetworkInterface1.getId()).andReturn("SampleRTBId").anyTimes();
    expect(adNetworkInterface1.getHttpResponseContent()).andReturn("SampleRTBResponseContent").anyTimes();
    expect(adNetworkInterface1.getRequestUrl()).andReturn("SampleRTBURL").anyTimes();
    ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
    thirdPartyAdResponse.adStatus = "AD";
    thirdPartyAdResponse.latency = 12;
    expect(adNetworkInterface1.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
    replay(adNetworkInterface1);
    ChannelSegmentEntity channelSegmentEntity = createMock(ChannelSegmentEntity.class);
    expect(channelSegmentEntity.getExternalSiteKey()).andReturn("ext").anyTimes();
    expect(channelSegmentEntity.getId()).andReturn("extId").anyTimes();
    replay(channelSegmentEntity);
    ChannelSegment channelSegment1 = httpRequestHandlerbase.new ChannelSegment(channelSegmentEntity, adNetworkInterface1, null, null);  
    httpRequestHandlerbase.rtbSegments.add(channelSegment1);
    httpRequestHandlerbase.writeLogs();
  }
  
}
