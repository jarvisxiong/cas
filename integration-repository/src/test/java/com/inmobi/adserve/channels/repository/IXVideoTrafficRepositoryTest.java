package com.inmobi.adserve.channels.repository;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.IXVideoTrafficEntity;
import com.inmobi.adserve.channels.query.IXVideoTrafficQuery;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, IXVideoTrafficRepository.class})
public class IXVideoTrafficRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final String siteId = "siteId";
        final int countryId = 15;
        final int trafficPercentage = 25;
        final boolean isActive = true;
        final Timestamp modifiedOn = new Timestamp(1234L);

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        final Logger mockLogger = createNiceMock(Logger.class);

        expect(mockNullAsZeroResultSetRow.getString("site_id")).andReturn(siteId).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("country_id")).andReturn(countryId).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("traffic_percentage")).andReturn(trafficPercentage).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("is_active")).andReturn(isActive).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final IXVideoTrafficRepository tested = new IXVideoTrafficRepository();
        MemberMatcher.field(ChannelRepository.class, "logger").set(tested, mockLogger);

        final DBEntity<IXVideoTrafficEntity, IXVideoTrafficQuery> entity = tested.buildObjectFromRow(null);
        final IXVideoTrafficEntity output = entity.getObject();

        assertThat(output.getSiteId(), is(equalTo(siteId)));
        assertThat(output.getCountryId(), is(equalTo(countryId)));
        assertThat(output.getTrafficPercentage(), is(equalTo((short) trafficPercentage)));
        assertThat(output.getIsActive(), is(equalTo(isActive)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final IXVideoTrafficRepository tested = new IXVideoTrafficRepository();
        final IXVideoTrafficEntity mockEntity = createMock(IXVideoTrafficEntity.class);

        expect(mockEntity.getIsActive()).andReturn(false).times(1).andReturn(true).times(1);

        replayAll();
        assertThat(tested.isObjectToBeDeleted(mockEntity), is(equalTo(true)));
        assertThat(tested.isObjectToBeDeleted(mockEntity), is(equalTo(false)));
        verifyAll();
    }
}
