package com.inmobi.adserve.channels.repository;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

import java.sql.Timestamp;

/**
 * Created by anshul.soni on 05/01/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, ChannelSegmentFeedbackRepository.class})
public class ChannelSegmentFeedbackRepositoryTest {
    @Test
    public void testBuildObjectFromRow() throws Exception {
        final String adGroupId = "fe3758c9bef141b7996e9bcf20816f40";
        final Timestamp modifiedOn = new Timestamp(1234L);
        final String advertiserId = "4028cb1e38411ed20138515af2b6025a";
        final double eCPM = 10000.0;
        final double fillRatio = 1.123875001123875E-6;
        final int todayImpressions = 0;
        final String expectedLogOutput = "adgroup id for the loaded channelSegmentFeedbackEntity is " + adGroupId;
        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        mockLogger.debug("result set is not null");
        expectLastCall().times(1);
        mockLogger.debug(expectedLogOutput);
        expectLastCall().times(1);

        expect(mockNullAsZeroResultSetRow.getString("ad_group_id")).andReturn(adGroupId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("advertiser_id")).andReturn(advertiserId).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("ecpm")).andReturn(eCPM).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("fill_ratio")).andReturn(fillRatio).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("today_impressions")).andReturn(todayImpressions).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final ChannelSegmentFeedbackRepository tested = new ChannelSegmentFeedbackRepository();
        MemberModifier.field(ChannelSegmentFeedbackRepository.class, "logger").set(tested, mockLogger);
        final DBEntity<ChannelSegmentFeedbackEntity, String> entity = tested.buildObjectFromRow(null);
        final ChannelSegmentFeedbackEntity output = entity.getObject();

        assertThat(output.getAdGroupId(), is(equalTo(adGroupId)));
        assertThat(output.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(output.getECPM(), is(equalTo(eCPM)));
        assertThat(output.getFillRatio(), is(equalTo(fillRatio)));
        assertThat(output.getTodayImpressions(), is(equalTo(todayImpressions)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final ChannelSegmentFeedbackRepository tested = new ChannelSegmentFeedbackRepository();

        final ChannelSegmentFeedbackEntity dummy1 = createMock(ChannelSegmentFeedbackEntity.class);
        expect(dummy1.getAdGroupId()).andReturn(null).times(1).andReturn("fe3758c9bef141b7996e9bcf20816f40").times(1);
        replayAll();
        assertThat(tested.isObjectToBeDeleted(dummy1), is(equalTo(true)));
        assertThat(tested.isObjectToBeDeleted(dummy1), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final ChannelSegmentFeedbackRepository tested = new ChannelSegmentFeedbackRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final ChannelSegmentFeedbackRepository tested = new ChannelSegmentFeedbackRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
