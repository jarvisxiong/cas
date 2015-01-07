package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.junit.Test;

import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

public class AdvertiserBurnLimitExceededFilterTest {

    @Test
    public void testFailedInFilterChannelSegmentPasses() throws Exception {
        double balance = 10;
        double revenue = 3;
        double revenueWindow = 3;   // Current value in config file

        ServerConfig mockConfig = createMock(ServerConfig.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);

        expect(mockConfig.getRevenueWindow()).andReturn(revenueWindow).anyTimes();
        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelFeedbackEntity.getBalance()).andReturn(balance).anyTimes();
        expect(mockChannelFeedbackEntity.getRevenue()).andReturn(revenue).anyTimes();
        replayAll();

        AdvertiserBurnLimitExceededFilter filter = new AdvertiserBurnLimitExceededFilter(null, mockConfig);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterChannelSegmentFails() throws Exception {
        double balance = 2;
        double revenue = 3;
        double revenueWindow = 3;   // Current value in config file

        ServerConfig mockConfig = createMock(ServerConfig.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);

        expect(mockConfig.getRevenueWindow()).andReturn(revenueWindow).anyTimes();
        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelFeedbackEntity.getBalance()).andReturn(balance).anyTimes();
        expect(mockChannelFeedbackEntity.getRevenue()).andReturn(revenue).anyTimes();
        replayAll();

        AdvertiserBurnLimitExceededFilter filter = new AdvertiserBurnLimitExceededFilter(null, mockConfig);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }

}
