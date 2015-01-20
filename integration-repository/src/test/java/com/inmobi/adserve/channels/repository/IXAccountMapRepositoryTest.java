package com.inmobi.adserve.channels.repository;


import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.sql.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 05/01/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, IXAccountMapRepository.class})
public class IXAccountMapRepositoryTest {
    @Test
    public void testBuildObjectFromRow() throws Exception {
        final Long rpNetworkId = 3288L;
        final Timestamp modifiedOn = new Timestamp(1409770206000L);
        final String inmobiAccountId = "efb24a8142e7429d8970e16823ac6e38";
        final String networkName = "WebMobLink RTB";
        final String networkType = "RTB";

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getLong("rp_network_id")).andReturn(rpNetworkId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("inmobi_account_id")).andReturn(inmobiAccountId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("network_name")).andReturn(networkName).times(1);
        expect(mockNullAsZeroResultSetRow.getString("network_type")).andReturn(networkType).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final IXAccountMapRepository tested = new IXAccountMapRepository();
        final DBEntity<IXAccountMapEntity, Long> entity = tested.buildObjectFromRow(null);
        final IXAccountMapEntity output = entity.getObject();

        assertThat(output.getRpNetworkId(), is(equalTo(rpNetworkId)));
        assertThat(output.getInmobiAccountId(), is(equalTo(inmobiAccountId)));
        assertThat(output.getNetworkName(), is(equalTo(networkName)));
        assertThat(output.getNetworkType(), is(equalTo(networkType)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));

        verifyAll();
        return;
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final IXAccountMapRepository tested = new IXAccountMapRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final IXAccountMapRepository tested = new IXAccountMapRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final IXAccountMapRepository tested = new IXAccountMapRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
