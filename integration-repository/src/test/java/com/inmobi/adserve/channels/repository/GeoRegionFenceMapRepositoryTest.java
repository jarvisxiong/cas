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
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, GeoRegionFenceMapRepository.class})
public class GeoRegionFenceMapRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {

        String geoRegionName = "FiftyMilesFromInMobiDataCentres";
        Long countryId = 94L;
        Long[] fenceIdsArray = new Long[]{5L, 6L, 777L, 7575L};
        Timestamp modifiedOn = new Timestamp(1234L);

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getString("geo_region_name")).andReturn(geoRegionName).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("country_id")).andReturn(countryId).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("fence_ids_list")).andReturn(fenceIdsArray).times(2);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final GeoRegionFenceMapRepository tested = new GeoRegionFenceMapRepository();
        final DBEntity<GeoRegionFenceMapEntity, String> entity = tested.buildObjectFromRow(null);
        final GeoRegionFenceMapEntity output = entity.getObject();

        assertThat(output.getGeoRegionName(), is(equalTo(geoRegionName)));
        assertThat(output.getCountryId(), is(equalTo(countryId)));
        assertThat(output.getFenceIdsList(), is(equalTo(Arrays.asList(fenceIdsArray))));
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
