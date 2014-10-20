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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ChannelRepository.class, ResultSetRow.class})
public class ChannelRepositoryTest {

	@Test
	public void testBuildObjectFromRowRequestCapIs0SiteInclusionIsTrue() throws Exception {
		final String sieJson = "{\"sites\":[\"abcd\",\"efgh\"],\"mode\":\"inclusion\"}";
		final Set<String> siteIE = new HashSet(Arrays.asList("abcd", "efgh"));

		final String id = "id";
		final Timestamp modifiedOn = new Timestamp(1234L);
		final String name = "name";
		final String accountId = "account_id";
		final String reportingApiKey = "reporting_api_key";
		final String reportingApiUrl = "reporting_api_url";
		final String username = "username";
		final String password = "password";
		final Boolean isTestMode = false;
		final Boolean isActive = true;
		final Long burstQps = 500L;
		final Long impressionCeil = 1000L;
		final Long impressionFloor = 100L;
		Long requestCap = 0L;
		final int priority = 5;
		final int demandSourceTypeId = 8;
		final int accountSegment = 1;

		final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

		expect(mockNullAsZeroResultSetRow.getString("id")).andReturn(id).times(1);
		expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
		expect(mockNullAsZeroResultSetRow.getString("name")).andReturn(name).times(1);
		expect(mockNullAsZeroResultSetRow.getString("account_id")).andReturn(accountId).times(1);
		expect(mockNullAsZeroResultSetRow.getString("reporting_api_key")).andReturn(reportingApiKey).times(1);
		expect(mockNullAsZeroResultSetRow.getString("reporting_api_url")).andReturn(reportingApiUrl).times(1);
		expect(mockNullAsZeroResultSetRow.getString("username")).andReturn(username).times(1);
		expect(mockNullAsZeroResultSetRow.getString("password")).andReturn(password).times(1);
		expect(mockNullAsZeroResultSetRow.getBoolean("is_test_mode")).andReturn(isTestMode).times(1);
		expect(mockNullAsZeroResultSetRow.getBoolean("is_active")).andReturn(isActive).times(1);
		expect(mockNullAsZeroResultSetRow.getLong("burst_qps")).andReturn(burstQps).times(1);
		expect(mockNullAsZeroResultSetRow.getLong("impression_ceil")).andReturn(impressionCeil).times(1);
		expect(mockNullAsZeroResultSetRow.getLong("impression_floor")).andReturn(impressionFloor).times(1);
		expect(mockNullAsZeroResultSetRow.getLong("request_cap")).andReturn(requestCap).times(1);
		expect(mockNullAsZeroResultSetRow.getInt("priority")).andReturn(priority).times(1);
		expect(mockNullAsZeroResultSetRow.getInt("demand_source_type_id")).andReturn(demandSourceTypeId).times(1);
		expect(mockNullAsZeroResultSetRow.getString("sie_json")).andReturn(sieJson).times(1);
		expect(mockNullAsZeroResultSetRow.getInt("account_segment")).andReturn(accountSegment).times(1);
		expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
				mockNullAsZeroResultSetRow).times(1);

		replayAll();

		final ChannelRepository tested = new ChannelRepository();
		final DBEntity<ChannelEntity, String> entity = tested.buildObjectFromRow(null);
		final ChannelEntity output = entity.getObject();
		final Timestamp outputModifiedOn = entity.getModifiedTime();

		if (requestCap == 0) {
			requestCap = Long.MAX_VALUE;
		}

		assertThat(output.getChannelId(), is(equalTo(id)));
		assertThat(output.getName(), is(equalTo(name)));
		assertThat(output.getAccountId(), is(equalTo(accountId)));
		assertThat(output.getReportingApiKey(), is(equalTo(reportingApiKey)));
		assertThat(output.getReportingApiUrl(), is(equalTo(reportingApiUrl)));
		assertThat(output.getUsername(), is(equalTo(username)));
		assertThat(output.getPassword(), is(equalTo(password)));
		assertThat(output.isTestMode(), is(equalTo(isTestMode)));
		assertThat(output.isActive(), is(equalTo(isActive)));
		assertThat(output.getBurstQps(), is(equalTo(burstQps)));
		assertThat(output.getImpressionCeil(), is(equalTo(impressionCeil)));
		assertThat(output.getImpressionFloor(), is(equalTo(impressionFloor)));
		assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));
		assertThat(output.getPriority(), is(equalTo(priority)));
		assertThat(output.getDemandSourceTypeId(), is(equalTo(demandSourceTypeId)));
		assertThat(output.getRequestCap(), is(equalTo(requestCap)));
		assertThat(output.getSitesIE(), is(siteIE));
		assertThat(output.isSiteInclusion(), is(equalTo(true)));
		assertThat(output.getAccountSegment(), is(equalTo(accountSegment)));
		assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

		verifyAll();
	}

	@Test
	public void getModeTest() throws Exception {
		final String workingSieJsonTrue = "{\"sites\":[\"abcd\",\"efgh\"],\"mode\":\"inclusion\"}";
		final String workingSieJsonFalse = "{\"sites\":[\"abcd\",\"efgh\"],\"mode\":\"exclusion\"}";
		final String faultySieJson = "NotAJson";

		final Logger mockLogger = createNiceMock(Logger.class);

		replayAll();

		final ChannelRepository tested = new ChannelRepository();
		MemberMatcher.field(ChannelRepository.class, "logger").set(tested, mockLogger);

		assertThat(tested.getMode(workingSieJsonTrue), is(equalTo(true)));
		assertThat(tested.getMode(workingSieJsonFalse), is(equalTo(false)));
		assertThat(tested.getMode(faultySieJson), is(equalTo(false)));

		verifyAll();
	}

	@Test
	public void getSitesTest() throws Exception {
		final String workingSieJson = "{\"sites\":[\"abcd\",\"efgh\"],\"mode\":\"inclusion\"}";
		final String faultySieJson = "NotAJson";
		final Set<String> output = new HashSet<String>(Arrays.asList("abcd", "efgh"));
		final Set<String> emptyOutput = new HashSet<String>();

		final Logger mockLogger = createNiceMock(Logger.class);

		replayAll();

		final ChannelRepository tested = new ChannelRepository();
		MemberMatcher.field(ChannelRepository.class, "logger").set(tested, mockLogger);

		assertThat(tested.getSites(workingSieJson), is(equalTo(output)));
		assertThat(tested.getSites(faultySieJson), is(equalTo(emptyOutput)));

		verifyAll();
	}


	@Test
	public void testIsObjectToBeDeleted() throws Exception {
		final ChannelRepository tested = new ChannelRepository();
		final ChannelEntity dummy = createMock(ChannelEntity.class);

		expect(dummy.getId()).andReturn(null).times(1).andReturn("Id").times(1);
		replayAll();

		assertThat(tested.isObjectToBeDeleted(dummy), is(equalTo(true)));
		assertThat(tested.isObjectToBeDeleted(dummy), is(equalTo(false)));
	}

	@Test
	public void testGetHashIndexKeyBuilder() throws Exception {
		final ChannelRepository tested = new ChannelRepository();
		assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
	}

	@Test
	public void testQueryUniqueResult() throws Exception {
		final ChannelRepository tested = new ChannelRepository();
		assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
	}
}
