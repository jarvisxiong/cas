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

import com.inmobi.adserve.channels.entity.SdkMraidMapEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SdkMraidMapRepository.class})
public class SdkMraidMapRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final String sdkVer = "a400";
        final String mraidPath = "test/mraid.js";
        final Timestamp modifiedOn = new Timestamp(1353954600000L);

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getString("name")).andReturn(sdkVer).times(1);
        expect(mockNullAsZeroResultSetRow.getString("mraid_path")).andReturn(mraidPath).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final SdkMraidMapRepository tested = new SdkMraidMapRepository();
        final DBEntity<SdkMraidMapEntity, String> entity = tested.buildObjectFromRow(null);
        final SdkMraidMapEntity output = entity.getObject();

        assertThat(output.getSdkName(), is(equalTo(sdkVer)));
        assertThat(output.getMraidPath(), is(equalTo(mraidPath)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final SdkMraidMapRepository tested = new SdkMraidMapRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final SdkMraidMapRepository tested = new SdkMraidMapRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final SdkMraidMapRepository tested = new SdkMraidMapRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}