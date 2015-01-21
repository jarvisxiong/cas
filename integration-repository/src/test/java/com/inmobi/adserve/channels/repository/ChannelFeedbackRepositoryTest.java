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

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 05/01/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, ChannelFeedbackRepository.class})
public class ChannelFeedbackRepositoryTest {
    @Test
    public void testBuildObjectFromRow() throws Exception {
        final String advertiserId = "a3d59e47436d47cab83847b29404e246";
        final Timestamp modifiedOn = new Timestamp(1234L);
        final Double totalInflow = 70000.0;
        final double totalBurn = 43459.524426;
        final double balance = 26540.475574;
        final int averageLatency = 2;
        final double revenue = 3330.5;
        final long totalImpressions = 10L;
        final long todayImpressions = 5L;
        final int todayRequests = 2;
        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        mockLogger.debug("result set is not null");
        expectLastCall().times(1);

        expect(mockNullAsZeroResultSetRow.getString("id")).andReturn(advertiserId).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("total_inflow")).andReturn(totalInflow).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("total_burn")).andReturn(totalBurn).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("balance")).andReturn(balance).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("average_latency")).andReturn(averageLatency).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("revenue")).andReturn(revenue).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("total_impressions")).andReturn(totalImpressions).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("today_impressions")).andReturn(todayImpressions).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("today_requests")).andReturn(todayRequests).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);



        replayAll();

        final ChannelFeedbackRepository tested = new ChannelFeedbackRepository();
        MemberModifier.field(ChannelFeedbackRepository.class, "logger").set(tested, mockLogger);
        final DBEntity<ChannelFeedbackEntity, String> entity = tested.buildObjectFromRow(null);
        final ChannelFeedbackEntity output = entity.getObject();

        assertThat(output.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(output.getTotalInflow(), is(equalTo(totalInflow)));
        assertThat(output.getTotalBurn(), is(equalTo(totalBurn)));
        assertThat(output.getBalance(), is(equalTo(balance)));
        assertThat(output.getAverageLatency(), is(equalTo(averageLatency)));
        assertThat(output.getTotalBurn(), is(equalTo(totalBurn)));
        assertThat(output.getTotalBurn(), is(equalTo(totalBurn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final ChannelFeedbackRepository tested = new ChannelFeedbackRepository();

        final ChannelFeedbackEntity dummy1 = createMock(ChannelFeedbackEntity.class);
        expect(dummy1.getAdvertiserId()).andReturn(null).times(1).andReturn("a3d59e47436d47cab83847b29404e246").times(1);
        replayAll();
        assertThat(tested.isObjectToBeDeleted(dummy1), is(equalTo(true)));
        assertThat(tested.isObjectToBeDeleted(dummy1), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final ChannelFeedbackRepository tested = new ChannelFeedbackRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final ChannelFeedbackRepository tested = new ChannelFeedbackRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
