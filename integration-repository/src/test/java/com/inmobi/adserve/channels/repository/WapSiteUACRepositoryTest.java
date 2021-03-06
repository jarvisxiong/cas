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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, WapSiteUACRepository.class})
public class WapSiteUACRepositoryTest {
    private static final long ANDROID_SITE_TYPE = 22;

    @Test
    public void testBuildObjectFromRow1() throws Exception {
        /**
         * Conditions/Branches followed: exchange_settings is 1 siteTypeId == ANDROID_SITE_TYPE && ... categoryList is
         * present transparencyEnabled is false siteBlindList is present logger is debug enabled
         */

        final String id = "id";
        final Timestamp modifiedOn = new Timestamp(10L);
        final String marketId = "marketId";
        final long siteTypeId = ANDROID_SITE_TYPE;
        final String contentRating = "Everyone";
        final String appType = "app_type";
        final String categories = "a, b,      c,";
        final boolean coppaEnabled = true;
        final Integer exchange_settings = 1;
        final Integer pubBlindArr[] = {4, 5};
        final Integer siteBlindArr[] = {6, 10};
        final boolean siteTransparencyEnabled = true;
        final String siteUrl = "site_url";
        final String siteName = "site_name";
        final String appTitle = "title";
        final String bundleId = "bundle_id";

        final String expectedContentRating = "4+";
        final List<String> expectedCategories = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        final List<Integer> expectedBlindList = new ArrayList<Integer>(Arrays.asList(siteBlindArr));

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        final Logger mockLogger = createNiceMock(Logger.class);

        expect(mockNullAsZeroResultSetRow.getString("id")).andReturn(id).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getString("market_id")).andReturn(marketId).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("site_type_id")).andReturn(siteTypeId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("content_rating")).andReturn(contentRating).times(1);
        expect(mockNullAsZeroResultSetRow.getString("app_type")).andReturn(appType).times(1);
        expect(mockNullAsZeroResultSetRow.getString("categories")).andReturn(categories).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("coppa_enabled")).andReturn(coppaEnabled).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("exchange_settings")).andReturn(exchange_settings).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("pub_blind_list")).andReturn(pubBlindArr).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("site_blind_list")).andReturn(siteBlindArr).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("is_site_transparent")).andReturn(siteTransparencyEnabled)
                .times(1);
        expect(mockNullAsZeroResultSetRow.getString("site_url")).andReturn(siteUrl).times(1);
        expect(mockNullAsZeroResultSetRow.getString("site_name")).andReturn(siteName).times(1);
        expect(mockNullAsZeroResultSetRow.getString("title")).andReturn(appTitle).times(1);
        expect(mockNullAsZeroResultSetRow.getString("bundle_id")).andReturn(bundleId).times(1);
        expect(mockLogger.isDebugEnabled()).andReturn(true).times(1);

        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final WapSiteUACRepository tested = new WapSiteUACRepository();
        MemberMatcher.field(WapSiteUACRepository.class, "logger").set(tested, mockLogger);

        final DBEntity<WapSiteUACEntity, String> entity = tested.buildObjectFromRow(null);
        final WapSiteUACEntity output = entity.getObject();
        final Timestamp outputModifiedOn = entity.getModifiedTime();

        assertThat(output.getId(), is(equalTo(id)));
        assertThat(output.getMarketId(), is(equalTo(marketId)));
        assertThat(output.getSiteTypeId(), is(equalTo(siteTypeId)));
        assertThat(output.isCoppaEnabled(), is(equalTo(coppaEnabled)));
        assertThat(output.getAppType(), is(equalTo(appType)));
        assertThat(output.getSiteUrl(), is(equalTo(siteUrl)));
        assertThat(output.getSiteName(), is(equalTo(siteName)));
        assertThat(output.getAppTitle(), is(equalTo(appTitle)));
        assertThat(output.getBundleId(), is(equalTo(bundleId)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));
        assertThat(output.getContentRating(), is(equalTo(expectedContentRating)));
        assertThat(output.getCategories(), is(equalTo(expectedCategories)));
        assertThat(output.isTransparencyEnabled(), is(equalTo(true)));
        assertThat(output.getBlindList(), is(equalTo(expectedBlindList)));
        assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testBuildObjectFromRow2() throws Exception {
        /**
         * Conditions/Branches followed: exchange_settings is not 1 siteTypeId != ANDROID_SITE_TYPE categoryList is not
         * present transparencyEnabled is false siteBlindList is not present but pubBlindList is logger is not debug
         * enabled
         */

        final String id = "id";
        final Timestamp modifiedOn = new Timestamp(1000L);
        final String marketId = "marketId";
        final long siteTypeId = 20;
        final String contentRating = "contentRating";
        final String appType = "app_type";
        final String categories = "a, b,      c,";
        final boolean coppaEnabled = true;
        final Integer exchange_settings = 0;
        final Integer pubBlindArr[] = {4, 5};
        final Integer siteBlindArr[] = {};
        final boolean siteTransparencyEnabled = false;
        final String siteUrl = "site_url";
        final String siteName = "site_name";
        final String appTitle = "title";
        final String bundleId = "bundle_id";

        final String expectedContentRating = contentRating;
        final List<String> expectedCategories = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        final List<Integer> expectedBlindList = new ArrayList<Integer>(Arrays.asList(pubBlindArr));

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        final Logger mockLogger = createMock(Logger.class);

        expect(mockNullAsZeroResultSetRow.getString("id")).andReturn(id).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expect(mockNullAsZeroResultSetRow.getString("market_id")).andReturn(marketId).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("site_type_id")).andReturn(siteTypeId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("content_rating")).andReturn(contentRating).times(1);
        expect(mockNullAsZeroResultSetRow.getString("app_type")).andReturn(appType).times(1);
        expect(mockNullAsZeroResultSetRow.getString("categories")).andReturn(categories).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("coppa_enabled")).andReturn(coppaEnabled).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("exchange_settings")).andReturn(exchange_settings).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("pub_blind_list")).andReturn(pubBlindArr).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("site_blind_list")).andReturn(siteBlindArr).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("is_site_transparent")).andReturn(siteTransparencyEnabled)
                .times(1);
        expect(mockNullAsZeroResultSetRow.getString("site_url")).andReturn(siteUrl).times(1);
        expect(mockNullAsZeroResultSetRow.getString("site_name")).andReturn(siteName).times(1);
        expect(mockNullAsZeroResultSetRow.getString("title")).andReturn(appTitle).times(1);
        expect(mockNullAsZeroResultSetRow.getString("bundle_id")).andReturn(bundleId).times(1);
        expect(mockLogger.isDebugEnabled()).andReturn(false).times(1);

        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final WapSiteUACRepository tested = new WapSiteUACRepository();
        MemberMatcher.field(WapSiteUACRepository.class, "logger").set(tested, mockLogger);

        final DBEntity<WapSiteUACEntity, String> entity = tested.buildObjectFromRow(null);
        final WapSiteUACEntity output = entity.getObject();
        final Timestamp outputModifiedOn = entity.getModifiedTime();

        assertThat(output.getId(), is(equalTo(id)));
        assertThat(output.getMarketId(), is(equalTo(marketId)));
        assertThat(output.getSiteTypeId(), is(equalTo(siteTypeId)));
        assertThat(output.isCoppaEnabled(), is(equalTo(coppaEnabled)));
        assertThat(output.getAppType(), is(equalTo(appType)));
        assertThat(output.getSiteUrl(), is(equalTo(siteUrl)));
        assertThat(output.getSiteName(), is(equalTo(siteName)));
        assertThat(output.getAppTitle(), is(equalTo(appTitle)));
        assertThat(output.getBundleId(), is(equalTo(bundleId)));
        assertThat(output.getModifiedOn(), is(equalTo(modifiedOn)));
        assertThat(output.getContentRating(), is(equalTo(expectedContentRating)));
        assertThat(output.getCategories(), is(equalTo(expectedCategories)));
        assertThat(output.isTransparencyEnabled(), is(equalTo(false)));
        assertThat(output.getBlindList(), is(equalTo(expectedBlindList)));
        assertThat(outputModifiedOn, is(equalTo(modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final WapSiteUACRepository tested = new WapSiteUACRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final WapSiteUACRepository tested = new WapSiteUACRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final WapSiteUACRepository tested = new WapSiteUACRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
