package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

public class AdvertiserDroppedInRtbBalanceFilterTest {

    @Test
    public void testFailedInFilterAdapterIsNotRtbNorIx() throws Exception {
        String advertiserId = "advertiserId";
        int rtbFilterAmount = 43;

        Configuration mockConfig = createMock(Configuration.class);
        ServerConfig mockServerConfig = createMock(ServerConfig.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getBoolean("isRtb", false)).andReturn(false).anyTimes();
        expect(mockConfig.getBoolean("isIx", false)).andReturn(false).anyTimes();
        expect(mockServerConfig.getRtbBalanceFilterAmount()).andReturn(rtbFilterAmount).anyTimes();
        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", "dcName", null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDroppedInRtbBalanceFilter filter = new AdvertiserDroppedInRtbBalanceFilter(null, advertiserIdConfigMap, mockServerConfig);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterAdapterPasses() throws Exception {
        String advertiserId = "advertiserId";
        int rtbFilterAmount = 43;
        double balance = 450L;

        Configuration mockConfig = createMock(Configuration.class);
        ServerConfig mockServerConfig = createMock(ServerConfig.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getBoolean("isRtb", false)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean("isIx", false)).andReturn(false).anyTimes();
        expect(mockServerConfig.getRtbBalanceFilterAmount()).andReturn(rtbFilterAmount).anyTimes();
        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelFeedbackEntity.getBalance()).andReturn(balance).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", "dcName", null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDroppedInRtbBalanceFilter filter = new AdvertiserDroppedInRtbBalanceFilter(null, advertiserIdConfigMap, mockServerConfig);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterAdapterFails() throws Exception {
        String advertiserId = "advertiserId";
        int rtbFilterAmount = 43;
        double balance = 4L;

        Configuration mockConfig = createMock(Configuration.class);
        ServerConfig mockServerConfig = createMock(ServerConfig.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getBoolean("isRtb", false)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean("isIx", false)).andReturn(false).anyTimes();
        expect(mockServerConfig.getRtbBalanceFilterAmount()).andReturn(rtbFilterAmount).anyTimes();
        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn(advertiserId).anyTimes();
        expect(mockChannelFeedbackEntity.getBalance()).andReturn(balance).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", "dcName", null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDroppedInRtbBalanceFilter filter = new AdvertiserDroppedInRtbBalanceFilter(null, advertiserIdConfigMap, mockServerConfig);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }

}
