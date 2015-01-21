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

import com.inmobi.adserve.channels.entity.GeoZipEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 29/12/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, GeoZipRepository.class})
public class GeoZipRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final Integer zipId = 201498;
        final Timestamp modifiedOn = new Timestamp(1234L);
        final String zipCode = "00130";

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getInt("id")).andReturn(zipId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("zipcode")).andReturn(zipCode).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final GeoZipRepository tested = new GeoZipRepository();
        final DBEntity<GeoZipEntity, Integer> entity = tested.buildObjectFromRow(null);
        final GeoZipEntity output = entity.getObject();

        assertThat(output.getZipId(), is(equalTo(zipId)));
        assertThat(output.getZipCode(), is(equalTo(zipCode)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final GeoZipRepository tested = new GeoZipRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final GeoZipRepository tested = new GeoZipRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final GeoZipRepository tested = new GeoZipRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
