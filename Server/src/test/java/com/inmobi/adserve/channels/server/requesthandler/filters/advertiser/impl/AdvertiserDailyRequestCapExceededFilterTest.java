package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.junit.Test;

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

public class AdvertiserDailyRequestCapExceededFilterTest {

    @Test
    public void testFailedInFilterChannelSegmentPasses() throws Exception {
        long todayRequests = 20000L;
        long requestCap = 300000000L;

        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelFeedbackEntity.getTodayRequests()).andReturn(todayRequests).anyTimes();
        expect(mockChannelEntity.getRequestCap()).andReturn(requestCap).anyTimes();
        replayAll();

        AdvertiserDailyRequestCapExceededFilter filter = new AdvertiserDailyRequestCapExceededFilter(null);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterChannelSegmentFails() throws Exception {
        long todayRequests = 20000000000000L;
        long requestCap = 300000000L;

        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);

        expect(mockChannelSegment.getChannelFeedbackEntity()).andReturn(mockChannelFeedbackEntity).anyTimes();
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockChannelFeedbackEntity.getTodayRequests()).andReturn(todayRequests).anyTimes();
        expect(mockChannelEntity.getRequestCap()).andReturn(requestCap).anyTimes();
        replayAll();

        AdvertiserDailyRequestCapExceededFilter filter = new AdvertiserDailyRequestCapExceededFilter(null);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(true)));
    }
}
