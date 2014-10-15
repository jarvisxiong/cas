package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
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
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SiteFilterRepository.class})
public class SiteFilterRepositoryTest {

    @Test
    public void testBuildObjectFromRow1() throws Exception {
        /**
         * Conditions/Branches followed:
         *  Rule Type is 4
         *  logger debug is enabled
         */

        String siteId = "siteId";
        String pubId = "pubId";
        int ruleTypeId = 4;
        Timestamp modifiedOn = new Timestamp(1234L);
        String[] filterData = {"tango", "down"};

        NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        Logger mockLogger = createNiceMock(Logger.class);

        expect(mockNullAsZeroResultSetRow.getString("site_id")).andReturn(siteId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("pub_id")).andReturn(pubId).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("rule_type_id")).andReturn(ruleTypeId).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("filter_data")).andReturn(filterData).times(1);
        expect(mockLogger.isDebugEnabled()).andReturn(true).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[]{ResultSetRow.class}, null)
                .andReturn(mockNullAsZeroResultSetRow).times(1);

        replayAll();

        SiteFilterRepository tested = new SiteFilterRepository();
        MemberModifier.field(ChannelRepository.class, "logger").set(tested, mockLogger);

        DBEntity<SiteFilterEntity, SiteFilterQuery> entity = tested.buildObjectFromRow(null);
        SiteFilterEntity output = entity.getObject();

        assertThat(output.getSiteId(), is(equalTo(siteId)));
        assertThat(output.getPubId(), is(equalTo(pubId)));
        assertThat(output.getRuleType(), is(equalTo(ruleTypeId)));
        assertThat(output.getModified_on(), is(equalTo(modifiedOn)));
        assertThat(output.getBlockedIabCategories(), is(equalTo(filterData)));

        verifyAll();
    }

    @Test
    public void testBuildObjectFromRow2() throws Exception {
        /**
         * Conditions/Branches followed:
         *  Rule Type is 6
         *  logger debug is false
         */

        String siteId = "siteId";
        String pubId = "pubId";
        int ruleTypeId = 6;
        Timestamp modifiedOn = new Timestamp(1234L);
        String[] filterData = {"tango", "down"};

        NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        Logger mockLogger = createMock(Logger.class);

        expect(mockNullAsZeroResultSetRow.getString("site_id")).andReturn(siteId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("pub_id")).andReturn(pubId).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("rule_type_id")).andReturn(ruleTypeId).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("filter_data")).andReturn(filterData).times(1);
        expect(mockLogger.isDebugEnabled()).andReturn(false).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[]{ResultSetRow.class}, null)
                .andReturn(mockNullAsZeroResultSetRow).times(1);

        replayAll();

        SiteFilterRepository tested = new SiteFilterRepository();
        MemberModifier.field(ChannelRepository.class, "logger").set(tested, mockLogger);

        DBEntity<SiteFilterEntity, SiteFilterQuery> entity = tested.buildObjectFromRow(null);
        SiteFilterEntity output = entity.getObject();

        assertThat(output.getSiteId(), is(equalTo(siteId)));
        assertThat(output.getPubId(), is(equalTo(pubId)));
        assertThat(output.getRuleType(), is(equalTo(ruleTypeId)));
        assertThat(output.getModified_on(), is(equalTo(modifiedOn)));
        assertThat(output.getBlockedAdvertisers(), is(equalTo(filterData)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        SiteFilterRepository tested = new SiteFilterRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        SiteFilterRepository tested = new SiteFilterRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }
}