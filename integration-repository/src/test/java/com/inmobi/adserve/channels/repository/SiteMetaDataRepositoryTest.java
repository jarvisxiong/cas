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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, SiteMetaDataRepository.class})
public class SiteMetaDataRepositoryTest {

    @Test
    public void testBuildObjectFromRow1() throws Exception {
        /**
         * Conditions/Branches followed: siteAdvertiserInclList is not null pubAdvertiserInclList is not null
         * selfServedAllowed is true
         */

        final String siteId = "siteId";
        final String pubId = "pubId";
        final Boolean allowSelfServe = true;
        final Timestamp modifiedOn = new Timestamp(1234L);
        final String[] siteAdvertiserInclList = {"tango", "down"};
        final String[] pubAdvertiserInclList = {"alpha", "beta"};

        final Set<String> expectedSiteAdvertiserInclList = new HashSet<String>(Arrays.asList(siteAdvertiserInclList));
        final Set<String> expectedPubAdvertiserInclList = new HashSet<String>(Arrays.asList(pubAdvertiserInclList));

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getString("site_id")).andReturn(siteId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("pub_id")).andReturn(pubId).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("allow_self_serve")).andReturn(allowSelfServe).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("site_advertiser_incl_list")).andReturn(siteAdvertiserInclList)
                .times(1);
        expect(mockNullAsZeroResultSetRow.getArray("pub_advertiser_incl_list")).andReturn(pubAdvertiserInclList).times(
                1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final SiteMetaDataRepository tested = new SiteMetaDataRepository();
        final DBEntity<SiteMetaDataEntity, String> entity = tested.buildObjectFromRow(null);
        final SiteMetaDataEntity output = entity.getObject();
        final Timestamp outputModifiedOn = entity.getModifiedTime();

        assertThat(output.getSiteId(), is(equalTo(siteId)));
        assertThat(output.getPubId(), is(equalTo(pubId)));
        assertThat(output.getAccountTypesAllowed(), is(equalTo(AccountType.SELF_SERVE)));
        assertThat(output.getModified_on(), is(equalTo(modifiedOn)));
        assertThat(output.getAdvertisersIncludedBySite(), is(equalTo(expectedSiteAdvertiserInclList)));
        assertThat(output.getAdvertisersIncludedByPublisher(), is(equalTo(expectedPubAdvertiserInclList)));
        assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testBuildObjectFromRow2() throws Exception {
        /**
         * Conditions/Branches followed: siteAdvertiserInclList is null pubAdvertiserInclList is null selfServedAllowed
         * is false
         */

        final String siteId = "siteId";
        final String pubId = "pubId";
        final Boolean allowSelfServe = false;
        final Timestamp modifiedOn = new Timestamp(1234L);
        final String[] siteAdvertiserInclList = null;
        final String[] pubAdvertiserInclList = null;

        final Set<String> expectedSiteAdvertiserInclList = new HashSet<>();
        final Set<String> expectedPubAdvertiserInclList = new HashSet<>();

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        expect(mockNullAsZeroResultSetRow.getString("site_id")).andReturn(siteId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("pub_id")).andReturn(pubId).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("allow_self_serve")).andReturn(allowSelfServe).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("site_advertiser_incl_list")).andReturn(siteAdvertiserInclList)
                .times(1);
        expect(mockNullAsZeroResultSetRow.getArray("pub_advertiser_incl_list")).andReturn(pubAdvertiserInclList).times(
                1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final SiteMetaDataRepository tested = new SiteMetaDataRepository();
        final DBEntity<SiteMetaDataEntity, String> entity = tested.buildObjectFromRow(null);
        final SiteMetaDataEntity output = entity.getObject();
        final Timestamp outputModifiedOn = entity.getModifiedTime();

        assertThat(output.getSiteId(), is(equalTo(siteId)));
        assertThat(output.getPubId(), is(equalTo(pubId)));
        assertThat(output.getAccountTypesAllowed(), is(equalTo(AccountType.MANAGED)));
        assertThat(output.getModified_on(), is(equalTo(modifiedOn)));
        assertThat(output.getAdvertisersIncludedBySite(), is(equalTo(expectedSiteAdvertiserInclList)));
        assertThat(output.getAdvertisersIncludedByPublisher(), is(equalTo(expectedPubAdvertiserInclList)));
        assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final SiteMetaDataRepository tested = new SiteMetaDataRepository();
        final SiteMetaDataEntity dummy = createMock(SiteMetaDataEntity.class);

        expect(dummy.getSiteId()).andReturn(null).times(1).andReturn("siteId").times(1);
        replayAll();

        assertThat(tested.isObjectToBeDeleted(dummy), is(equalTo(true)));
        assertThat(tested.isObjectToBeDeleted(dummy), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final SiteMetaDataRepository tested = new SiteMetaDataRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final SiteMetaDataRepository tested = new SiteMetaDataRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
