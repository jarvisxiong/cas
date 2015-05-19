package com.inmobi.adserve.channels.repository;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.CcidMapEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, CcidMapRepository.class})
public class CcidMapRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final Integer ccid = 12345;
        final String country = "Sealand";
        final String carrier = "SMTL";

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getInt("country_carrier_id")).andReturn(ccid).times(1);
        expect(mockNullAsZeroResultSetRow.getString("country")).andReturn(country).times(1);
        expect(mockNullAsZeroResultSetRow.getString("carrier")).andReturn(carrier).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final CcidMapRepository tested = new CcidMapRepository();
        final DBEntity<CcidMapEntity, Integer> entity = tested.buildObjectFromRow(null);
        final CcidMapEntity output = entity.getObject();

        assertThat(output.getCountryCarrierId(), is(equalTo(ccid)));
        assertThat(output.getCountry(), is(equalTo(country)));
        assertThat(output.getCarrier(), is(equalTo(carrier)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final CcidMapRepository tested = new CcidMapRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final CcidMapRepository tested = new CcidMapRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final CcidMapRepository tested = new CcidMapRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}