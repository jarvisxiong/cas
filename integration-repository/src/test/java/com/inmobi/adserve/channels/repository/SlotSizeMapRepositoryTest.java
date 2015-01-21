package com.inmobi.adserve.channels.repository;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.awt.Dimension;
import java.sql.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 30/12/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SlotSizeMapRepository.class})
public class SlotSizeMapRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final Short slotId = 14;
        final Timestamp modifiedOn = new Timestamp(1353954600000L);
        final Integer height = 90;
        final Integer width = 1024;
        final Dimension dimension = new Dimension(width, height);

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getInt("id")).andReturn((int) slotId).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("height")).andReturn(height).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("width")).andReturn(width).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final SlotSizeMapRepository tested = new SlotSizeMapRepository();
        final DBEntity<SlotSizeMapEntity, Short> entity = tested.buildObjectFromRow(null);
        final SlotSizeMapEntity output = entity.getObject();

        assertThat(output.getId(), is(equalTo(slotId)));
        assertThat(output.getDimension(), is(equalTo(dimension)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final SlotSizeMapRepository tested = new SlotSizeMapRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final SlotSizeMapRepository tested = new SlotSizeMapRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final SlotSizeMapRepository tested = new SlotSizeMapRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
