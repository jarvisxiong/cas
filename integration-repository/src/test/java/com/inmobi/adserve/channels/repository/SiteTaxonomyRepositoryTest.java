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
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SiteTaxonomyRepository.class})
public class SiteTaxonomyRepositoryTest {

  @Test
  public void testBuildObjectFromRow() throws Exception {
    final int id = 10;
    final String name = "name";
    final int parentId = 13;
    final Timestamp modifiedOn = new Timestamp(1000L);
    final String expectedId = String.valueOf(id);
    final String expectedParentId = String.valueOf(parentId);

    final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
    final Logger mockLogger = createMock(Logger.class);

    expect(mockNullAsZeroResultSetRow.getInt("id")).andReturn(id).times(1);
    expect(mockNullAsZeroResultSetRow.getString("name")).andReturn(name).times(1);
    expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
    expect(mockNullAsZeroResultSetRow.getInt("parent_id")).andReturn(parentId).times(1);
    mockLogger.debug("Id for the loaded siteTaxonomyEntity is " + id);
    expectLastCall().times(1);

    expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
        mockNullAsZeroResultSetRow).times(1);

    replayAll();

    final SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
    MemberMatcher.field(SiteTaxonomyRepository.class, "logger").set(tested, mockLogger);

    final DBEntity<SiteTaxonomyEntity, String> entity = tested.buildObjectFromRow(null);
    final SiteTaxonomyEntity output = entity.getObject();
    final Timestamp outputModifiedOn = entity.getModifiedTime();
    assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

    assertThat(output.getId(), is(equalTo(expectedId)));
    assertThat(output.getName(), is(equalTo(name)));
    assertThat(output.getParentId(), is(equalTo(expectedParentId)));

    verifyAll();
  }

  @Test
  public void testIsObjectToBeDeleted() throws Exception {
    final SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
    assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
  }

  @Test
  public void testGetHashIndexKeyBuilder() throws Exception {
    final SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
    assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
  }

  @Test
  public void testQueryUniqueResult() throws Exception {
    final SiteTaxonomyRepository tested = new SiteTaxonomyRepository();
    assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
  }
}
