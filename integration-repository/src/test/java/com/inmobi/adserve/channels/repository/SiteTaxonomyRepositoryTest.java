package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Timestamp;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SiteTaxonomyRepository.class})
public class SiteTaxonomyRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        int id = 10;
        String name = "name";
        int parentId = 13;
        Timestamp modifiedOn = new Timestamp(1000L);
        String expectedId = String.valueOf(id);
        String expectedParentId = String.valueOf(parentId);

        NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        Logger mockLogger = createMock(Logger.class);

        expect(mockNullAsZeroResultSetRow.getInt("id")).andReturn(id).times(1);
        expect(mockNullAsZeroResultSetRow.getString("name")).andReturn(name).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("parent_id")).andReturn(parentId).times(1);
        mockLogger.debug("Id for the loaded siteTaxonomyEntity is " + id);
        expectLastCall().times(1);

        expectNew(NullAsZeroResultSetRow.class, new Class[]{ResultSetRow.class}, null)
                .andReturn(mockNullAsZeroResultSetRow).times(1);

        replayAll();

        SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
        MemberModifier.field(SiteTaxonomyRepository.class, "logger").set(tested, mockLogger);

        DBEntity<SiteTaxonomyEntity, String> entity = tested.buildObjectFromRow(null);
        SiteTaxonomyEntity output = entity.getObject();
        Timestamp outputModifiedOn = entity.getModifiedTime();
        assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

        assertThat(output.getId(), is(equalTo(expectedId)));
        assertThat(output.getName(), is(equalTo(name)));
        assertThat(output.getParentId(), is(equalTo(expectedParentId)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}