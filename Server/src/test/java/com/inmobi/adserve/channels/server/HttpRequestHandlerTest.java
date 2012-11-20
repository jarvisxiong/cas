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
  
  public void prepareConfig() throws Exception {
    FileWriter fstream = new FileWriter("/tmp/HTTpChannel-server.properties");
    BufferedWriter out = new BufferedWriter(fstream);
    out.write("log4j.logger.app = DEBUG, channel\n");
    out.write("log4j.additivity.app = false\n");
    out.write("log4j.appender.channel=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.channel.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.channel.DatePattern='.'yyyy-MM-dd-HH\n");
    channelFile = "/tmp/channel.log." + System.currentTimeMillis();
    out.write("log4j.appender.channel.File=" + channelFile + "\n");

    out.write("log4j.logger.app = DEBUG, rr\n");
    out.write("log4j.additivity.rr.app = false\n");
    out.write("log4j.appender.rr=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.rr.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.rr.DatePattern='.'yyyy-MM-dd-HH\n");
    System.out.println("here rr file name is " + rrFile);
    rrFile = "/tmp/rr.log." + System.currentTimeMillis();
    
    out.write("log4j.logger.app = DEBUG, debug\n");
    out.write("log4j.additivity.rr.app = false\n");
    out.write("log4j.appender.rr=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.rr.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.rr.DatePattern='.'yyyy-MM-dd-HH\n");
    debugFile = "/tmp/debug.log." + System.currentTimeMillis();
    System.out.println("here debug file name is " + debugFile);

    
    out.write("log4j.appender.rr.File = " + rrFile + "\n");
    out.write("log4j.category.debug = DEBUG,debug\n");
    out.write("log4j.category.rr = DEBUG,rr\n");
    out.write("log4j.category.channel = DEBUG,channel\n");
    out.write("server.percentRollout=100 \nserver.siteType=PERFORMANCE,FAMILYSAFE,MATURE\n");
    out.write("server.enableDatabusLogging = true\n");
    out.write("server.enableFileLogging = true \n");
    out.write("server.sampledadvertisercount = 2");
    out.write("\nserver.maxconnections=100\n");
    out.write("logger.rr = rr \n");
    out.write("logger.channel = channel");
    out.write("\n logger.debug = debug \n");
    out.write("logger.advertiser = advertiser\n");
    out.write("logger.sampledadvertiser = sampledadvertiser");
    out.write("\nlogger.loggerConf = /opt/mkhoj/conf/cas/channel-server.properties\n");
    out.close();
  }
  public void setUp() throws Exception {
    prepareConfig();
    if(count == 0) {
      config = ConfigurationLoader.getInstance("/tmp/HTTpChannel-server.properties");
      count++;
    }
    Configuration mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/tmp/channel-server.properties").anyTimes();
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    InspectorStats.initializeWorkflow("WorkFlow");
    HttpRequestHandler.init(config, (ChannelAdGroupRepository) null, (InspectorStats) null, (ClientBootstrap) null, (ClientBootstrap) null, null, null, null,
        null);
    AbstractMessagePublisher mockAbstractMessagePublisher = createMock(AbstractMessagePublisher.class);
    Logging.init(mockAbstractMessagePublisher, "cas-rr", "cas-channel", "cas-advertisement", config.serverConfiguration());
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
