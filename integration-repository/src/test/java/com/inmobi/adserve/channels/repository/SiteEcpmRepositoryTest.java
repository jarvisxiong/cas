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
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SiteEcpmRepository.class})
public class SiteEcpmRepositoryTest {

  @Test
  public void testBuildObjectFromRow() throws Exception {
    final String siteId = "siteId";
    final int countryId = 15;
    final int osId = 3;
    final double ecpm = 25.0;
    final double networkEcpm = 5.0;
    final Timestamp modifiedOn = new Timestamp(1234L);

    final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

    expect(mockNullAsZeroResultSetRow.getString("site_id")).andReturn(siteId).times(1);
    expect(mockNullAsZeroResultSetRow.getInt("country_id")).andReturn(countryId).times(1);
    expect(mockNullAsZeroResultSetRow.getInt("os_id")).andReturn(osId).times(1);
    expect(mockNullAsZeroResultSetRow.getDouble("ecpm")).andReturn(ecpm).times(1);
    expect(mockNullAsZeroResultSetRow.getDouble("network_ecpm")).andReturn(networkEcpm).times(1);
    expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
    expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
        mockNullAsZeroResultSetRow).times(1);

    replayAll();

    final SiteEcpmRepository tested = new SiteEcpmRepository();
    final DBEntity<SiteEcpmEntity, SiteEcpmQuery> entity = tested.buildObjectFromRow(null);
    final SiteEcpmEntity output = entity.getObject();

    assertThat(output.getSiteId(), is(equalTo(siteId)));
    assertThat(output.getCountryId(), is(equalTo(countryId)));
    assertThat(output.getOsId(), is(equalTo(osId)));
    assertThat(output.getEcpm(), is(equalTo(ecpm)));
    assertThat(output.getNetworkEcpm(), is(equalTo(networkEcpm)));
    assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));

    verifyAll();
  }

  @Test
  public void testIsObjectToBeDeleted() throws Exception {
    final SiteEcpmRepository tested = new SiteEcpmRepository();
    final SiteEcpmEntity mockSiteEcpmEntity = createMock(SiteEcpmEntity.class);

    final Timestamp fourDayOldTimeStamp = new Timestamp(DateUtils.addDays(new Date(), -4).getTime());
    final Timestamp twoDayOldTimeStamp = new Timestamp(DateUtils.addDays(new Date(), -2).getTime());

    expect(mockSiteEcpmEntity.getModifiedOn()).andReturn(fourDayOldTimeStamp).times(1).andReturn(twoDayOldTimeStamp)
        .times(1);

    replayAll();

    assertThat(tested.isObjectToBeDeleted(mockSiteEcpmEntity), is(equalTo(true)));
    assertThat(tested.isObjectToBeDeleted(mockSiteEcpmEntity), is(equalTo(false)));

    verifyAll();
  }

  @Test
  public void testGetHashIndexKeyBuilder() throws Exception {
    final SiteEcpmRepository tested = new SiteEcpmRepository();
    assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
  }

  @Test
  public void testQueryUniqueResult() throws Exception {
    final SiteEcpmRepository tested = new SiteEcpmRepository();
    assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
  }
}
