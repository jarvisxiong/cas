package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplierTest;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.casthrift.CreativeType;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class LoggingTest extends TestCase {
    ConfigurationLoader      config;
    Configuration            mockConfig;
    AbstractMessagePublisher dataBusPublisher;
    Set<String>              emptySet = new HashSet<String>();

    @Override
    public void setUp() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getString("sampledadvertiser")).andReturn("sampledadvertiser").anyTimes();
        expect(mockConfig.getBoolean("enableFileLogging")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean("enableDatabusLogging")).andReturn(true).anyTimes();
        expect(mockConfig.getInt("sampledadvertisercount")).andReturn(3).anyTimes();
        replay(mockConfig);
        AbstractMessagePublisher dataBusPublisher = createMock(AbstractMessagePublisher.class);
        dataBusPublisher.publish(isA(String.class), isA(Message.class));
        EasyMock.expectLastCall().times(4);
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
        ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(
                ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId",
                        "channelId", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId",
                        slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false,
                        false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false,
                        emptySet, 0));
        List<ChannelSegment> rankList = createMock(ArrayList.class);
        AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.adStatus = "AD";
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(CreativeType.BANNER).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork1").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("DummyResponsecontent").anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        replay(mockAdnetworkInterface);
        ChannelSegment channelSegment = new ChannelSegment(channelSegmentEntity, null, null, null, null,
                mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(1).anyTimes();
        replay(rankList);
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }

    @Test
    public void testsampledAdvertisingLoggingWithResponseAsEmptyString() {
        Long[] rcList = null;
        Long[] tags = null;
        Timestamp modified_on = null;
        Long[] slotIds = null;
        Integer[] siteRatings = null;
        ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(
                ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId",
                        "channelId", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId",
                        slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false,
                        false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false,
                        emptySet, 0));
        List<ChannelSegment> rankList = createMock(ArrayList.class);
        AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.adStatus = "AD";
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("").anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        replay(mockAdnetworkInterface);
        ChannelSegment channelSegment = new ChannelSegment(channelSegmentEntity, null, null, null, null,
                mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(1).anyTimes();
        replay(rankList);
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }

    @Test
    public void testsampledAdvertisingLoggingWithRequestUrlAsEmptyString() {
        Long[] rcList = null;
        Long[] tags = null;
        Timestamp modified_on = null;
        Long[] slotIds = null;
        Integer[] siteRatings = null;
        ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(
                ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId",
                        "channelId", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId",
                        slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false,
                        false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false,
                        emptySet, 0));
        List<ChannelSegment> rankList = createMock(ArrayList.class);
        AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.adStatus = "AD";
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("").anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        replay(mockAdnetworkInterface);
        ChannelSegment channelSegment = new ChannelSegment(channelSegmentEntity, null, null, null, null,
                mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(1).anyTimes();
        replay(rankList);
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }

    @Test
    public void testsampledAdvertisingLoggingForZeroSampleOnDatabus() {
        Long[] rcList = null;
        Long[] tags = null;
        Timestamp modified_on = null;
        Long[] slotIds = null;
        Integer[] siteRatings = null;
        ChannelSegmentEntity channelSegmentEntity = new ChannelSegmentEntity(
                ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("advertiserId", "adgroupId", "adId",
                        "channelId", 1, rcList, tags, true, true, "externalSiteKey", modified_on, "campaignId",
                        slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false, false, false,
                        false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, false,
                        emptySet, 0));
        List<ChannelSegment> rankList = createMock(ArrayList.class);
        AdNetworkInterface mockAdnetworkInterface = createMock(DCPTapitAdNetwork.class);
        ThirdPartyAdResponse thirdPartyAdResponse = new ThirdPartyAdResponse();
        thirdPartyAdResponse.adStatus = "AD";
        expect(mockAdnetworkInterface.getResponseStruct()).andReturn(thirdPartyAdResponse).anyTimes();
        expect(mockAdnetworkInterface.getName()).andReturn("DummyAdNetwork2").anyTimes();
        expect(mockAdnetworkInterface.getRequestUrl()).andReturn("url").anyTimes();
        expect(mockAdnetworkInterface.getHttpResponseContent()).andReturn("response").anyTimes();
        expect(mockAdnetworkInterface.getCreativeType()).andReturn(CreativeType.BANNER).anyTimes();
        expect(mockAdnetworkInterface.isRtbPartner()).andReturn(false).anyTimes();
        replay(mockAdnetworkInterface);
        ChannelSegment channelSegment = new ChannelSegment(channelSegmentEntity, null, null, null, null,
                mockAdnetworkInterface, 0);
        expect(rankList.get(0)).andReturn(channelSegment).anyTimes();
        expect(rankList.get(1)).andReturn(channelSegment).anyTimes();
        expect(rankList.get(2)).andReturn(channelSegment).anyTimes();
        expect(rankList.get(3)).andReturn(channelSegment).anyTimes();
        expect(rankList.size()).andReturn(4).anyTimes();
        replay(rankList);
        Logging.sampledAdvertiserLogging(rankList, mockConfig);
    }
}
