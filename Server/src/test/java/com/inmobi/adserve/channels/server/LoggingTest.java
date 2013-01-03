package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class LoggingTest extends TestCase {
  DebugLogger logger;
  ConfigurationLoader config;
  Configuration mockConfig;
  AbstractMessagePublisher dataBusPublisher;

  public void setUp() {
    mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties").anyTimes();
    expect(mockConfig.getString("sampledadvertiser")).andReturn("sampledadvertiser").anyTimes();
    expect(mockConfig.getBoolean("enableFileLogging")).andReturn(true).anyTimes();
    expect(mockConfig.getBoolean("enableDatabusLogging")).andReturn(true).anyTimes();
    expect(mockConfig.getInt("sampledadvertisercount")).andReturn(3).anyTimes();
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    logger = new DebugLogger();
    AbstractMessagePublisher dataBusPublisher = createMock(AbstractMessagePublisher.class);
    dataBusPublisher.publish(isA(String.class), isA(Message.class));
    EasyMock.expectLastCall().times(3);
    replay(dataBusPublisher);
    Logging.init(dataBusPublisher, "null", "null", "null", mockConfig);
  }

  @Test
  public void testsampledAdvertisingLogging() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null, null, true, false, false, false);
    List<ChannelSegment> rankList = createMock(ArrayList.class);
    AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
    ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
    thirdPartyAdResponse.adStatus = "AD";
    expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
    expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork1").anyTimes();
    expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
    expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("DummyResponsecontent").anyTimes();
    replay(mockAdnetworkInterface);
    ChannelSegment channelSegment = createMock(ChannelSegment.class);
    channelSegment.adNetworkInterface = mockAdnetworkInterface;
    channelSegment.channelSegmentEntity = channelSegmentEntity;
    expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
    expect(rankList.size()).andReturn(1).anyTimes();
    replay(rankList);
    Logging.sampledAdvertiserLogging(rankList, logger, mockConfig);
  }

  @Test
  public void testsampledAdvertisingLoggingWithResponseAsEmptyString() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null, null, true, false, false, false);
    List<ChannelSegment> rankList = createMock(ArrayList.class);
    AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
    ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
    thirdPartyAdResponse.adStatus = "AD";
    expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
    expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
    expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
    expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("").anyTimes();
    replay(mockAdnetworkInterface);
    ChannelSegment channelSegment = createMock(ChannelSegment.class);
    channelSegment.adNetworkInterface = mockAdnetworkInterface;
    channelSegment.channelSegmentEntity = channelSegmentEntity;
    expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
    expect(rankList.size()).andReturn(1).anyTimes();
    replay(rankList);
    Logging.sampledAdvertiserLogging(rankList, logger, mockConfig);
  }

  @Test
  public void testsampledAdvertisingLoggingWithRequestUrlAsEmptyString() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null, null, true, false, false, false);
    List<ChannelSegment> rankList = createMock(ArrayList.class);
    AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
    ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
    thirdPartyAdResponse.adStatus = "AD";
    expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
    expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
    expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
    expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("").anyTimes();
    replay(mockAdnetworkInterface);
    ChannelSegment channelSegment = createMock(ChannelSegment.class);
    channelSegment.adNetworkInterface = mockAdnetworkInterface;
    channelSegment.channelSegmentEntity = channelSegmentEntity;
    expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
    expect(rankList.size()).andReturn(1).anyTimes();
    replay(rankList);
    Logging.sampledAdvertiserLogging(rankList, logger, mockConfig);
  }

  @Test
  public void testsampledAdvertisingLoggingForZeroSampleOnDatabus() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null, null, true, false, false, false);
    List<ChannelSegment> rankList = createMock(ArrayList.class);
    AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
    ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
    thirdPartyAdResponse.adStatus = "AD";
    expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
    expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
    expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
    expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("response").anyTimes();
    replay(mockAdnetworkInterface);
    ChannelSegment channelSegment = createMock(ChannelSegment.class);
    channelSegment.adNetworkInterface = mockAdnetworkInterface;
    channelSegment.channelSegmentEntity = channelSegmentEntity;
    expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
    expect(rankList.get(1)).andReturn(channelSegment).anyTimes();
    expect(rankList.get(2)).andReturn(channelSegment).anyTimes();
    expect(rankList.get(3)).andReturn(channelSegment).anyTimes();
    expect(rankList.size()).andReturn(4).anyTimes();
    replay(rankList);
    Logging.sampledAdvertiserLogging(rankList, logger, mockConfig);
  }
}
