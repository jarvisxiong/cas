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
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

public class AdvertiserDetailsInvalidFilterTest {

    @Test
    public void testFailedInFilterAdapterNotFound() throws Exception {
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelEntity.getAccountId()).andReturn(null).anyTimes();
        replayAll();

        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();

        AdvertiserDetailsInvalidFilter filter = new AdvertiserDetailsInvalidFilter(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterNoHostFound() throws Exception {
        String advertiserId = "advertiserId";
        String dcName = "dcName";

        Configuration mockConfig = createMock(Configuration.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getString("host." + dcName)).andReturn(null).anyTimes();
        expect(mockConfig.getString("host.default")).andReturn(null).anyTimes();
        expect(mockConfig.getString("host")).andReturn(null).anyTimes();
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelEntity.getAccountId()).andReturn(advertiserId).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", dcName, null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDetailsInvalidFilter filter = new AdvertiserDetailsInvalidFilter(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterHostIsNA() throws Exception {
        String advertiserId = "advertiserId";
        String dcName = "dcName";

        Configuration mockConfig = createMock(Configuration.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getString("host." + dcName)).andReturn(null).anyTimes();
        expect(mockConfig.getString("host.default")).andReturn(null).anyTimes();
        expect(mockConfig.getString("host")).andReturn("NA").anyTimes();
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelEntity.getAccountId()).andReturn(advertiserId).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", dcName, null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDetailsInvalidFilter filter = new AdvertiserDetailsInvalidFilter(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterAdapterIsNotActive() throws Exception {
        String advertiserId = "advertiserId";
        String dcName = "dcName";

        Configuration mockConfig = createMock(Configuration.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getString("host." + dcName)).andReturn(null).anyTimes();
        expect(mockConfig.getString("host.default")).andReturn(null).anyTimes();
        expect(mockConfig.getString("host")).andReturn("SomeHostName").anyTimes();
        expect(mockConfig.getString("status", "on")).andReturn("off").anyTimes();
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelEntity.getAccountId()).andReturn(advertiserId).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", dcName, null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDetailsInvalidFilter filter = new AdvertiserDetailsInvalidFilter(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterAdapterPasses() throws Exception {
        String advertiserId = "advertiserId";
        String dcName = "dcName";

        Configuration mockConfig = createMock(Configuration.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getString("host." + dcName)).andReturn(null).anyTimes();
        expect(mockConfig.getString("host.default")).andReturn(null).anyTimes();
        expect(mockConfig.getString("host")).andReturn("SomeHostName").anyTimes();
        expect(mockConfig.getString("status", "on")).andReturn("on").anyTimes();
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelEntity.getAccountId()).andReturn(advertiserId).anyTimes();
        replayAll();

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, "adapterName", dcName, null);
        Map<String, AdapterConfig> advertiserIdConfigMap = new HashMap<>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserDetailsInvalidFilter filter = new AdvertiserDetailsInvalidFilter(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

}
