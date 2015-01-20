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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 07/01/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, ChannelAdGroupRepository.class})
public class ChannelAdGroupRepositoryTest {
    @Test
    public void testBuildObjectFromRow() throws Exception {
        final String adgroupId = "5224e8a0d0d74cff98f7a2a63bb6d0f6";
        final Timestamp modifyTime = new Timestamp(1406721731L);
        final String advertiserId = "49ab35303e3e4c658495bc68a4f3a574";
        final String[] adIds = new String[] {"622ddb6a0a0d4c30b59345d3b6ae9e7b"};
        final String channelId = "8aa8000f44f296c801466b803966000d";
        final String externalSiteKey = "RPnew_Germany_Android_Banner_FS_RON";
        final String campaignId = "a43e482f776f4cfca6f4033bb62133d2";
        final Long[] adIncIds = new Long[] {3338432L};
        final long adgroupIncId = 962882L;
        final boolean status = true;
        final String pricingModel = "CPm";
        final boolean isTestMode = false;
        final Integer[] siteRatings = new Integer[] {2};
        final Long[] rcList = new Long[] {53L};
        final Long[] slotIds = new Long[] {4L, 9L, 15L};
        final Integer[] creativeTypes = new Integer[] {0};
        final Long[] tags = new Long[] {1L, 2L};
        final Integer[] segmentFlags = new Integer[] {1, 2, 3, 9, 10};
        final long platformTargeting = 0L;
        final int targetingPlatform = 3;
        final ArrayList<Integer> targetingPlatformResult = new ArrayList<Integer>(Arrays.asList(1, 2));
        final String osVersionTargeting = "{os:[{id:5,incl:true}]}";
        final List<Integer> osIdsResult = new ArrayList<Integer>(Arrays.asList(5));
        final String additionalParams =
                "{57:161140,35:161098,36:161132,33:161098,34:161112,11:161098,38:161100,20:161110,64:161154,65:161148,60:161138,67:161154,site:38314,66:161148,69:161148,68:161136,27:161144,3:161102,2:161106,1:161158,10:161116,7:161106,6:161136,32:161146,5:161098,4:161154,31:161112,70:161148,9:161134,71:161148,8:161098,72:161150,73:161132,74:161132,50:161120,999:161158}";
        final JSONObject additionalParamsResult = new JSONObject(additionalParams);
        final Integer[] catTax = new Integer[] {};
        final Long[] catTaxResult = new Long[0];
        final String sIEJson = "{\"sites\": [\"93cda0ada1e5427694acb29efc0f29a2\"], \"mode\": \"exclusion\"}";
        final Set<String> sitesIEResult = new HashSet<String>(Arrays.asList("93cda0ada1e5427694acb29efc0f29a2"));
        final boolean siteInclusionResult = false;
        final long impressionCeil = 100000000L;
        final String manufModelTargeting = "{manuf:[{id:16,modelIds:[3],incl:true},{id:5,modelIds:[1],incl:true}]}";

        final List<Integer> manufModelTargetingResult = new ArrayList<Integer>(Arrays.asList(3, 1));
        final double ecpmBoost = 0.0;
        final Long ecmpBoostTimeInMilli = 1385503110L;
        final Timestamp eCPMBoostDate = new Timestamp(ecmpBoostTimeInMilli);
        final Long[] tod = new Long[] {};
        final int dst = 2;
        final long campaignIncId = 123502L;
        final String expectedLogOutput = "Adding adgroup " + adgroupId + " to channel segment repository";

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        mockLogger.debug(expectedLogOutput);
        expectLastCall().times(1);

        expect(mockNullAsZeroResultSetRow.getString("adgroup_id")).andReturn(adgroupId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("advertiser_id")).andReturn(advertiserId).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("ad_ids")).andReturn(adIds).times(1);
        expect(mockNullAsZeroResultSetRow.getString("channel_id")).andReturn(channelId).times(1);
        expect(mockNullAsZeroResultSetRow.getString("external_site_key")).andReturn(externalSiteKey).times(1);
        expect(mockNullAsZeroResultSetRow.getString("campaign_id")).andReturn(campaignId).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("ad_inc_ids")).andReturn(adIncIds).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("adgroup_inc_id")).andReturn(adgroupIncId).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("status")).andReturn(status).times(1);
        expect(mockNullAsZeroResultSetRow.getString("pricing_model")).andReturn(pricingModel).times(1);
        expect(mockNullAsZeroResultSetRow.getBoolean("is_test_mode")).andReturn(isTestMode).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("site_ratings")).andReturn(siteRatings).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("rc_list")).andReturn(rcList).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("slot_ids")).andReturn(slotIds).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("creative_types")).andReturn(creativeTypes).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("tags")).andReturn(tags).times(2);
        expect(mockNullAsZeroResultSetRow.getArray("segment_flags")).andReturn(segmentFlags).times(2);
        expect(mockNullAsZeroResultSetRow.getLong("platform_targeting_int")).andReturn(platformTargeting).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("targeting_platform")).andReturn(targetingPlatform).times(1);
        expect(mockNullAsZeroResultSetRow.getString("os_version_targeting")).andReturn(osVersionTargeting).times(1);
        expect(mockNullAsZeroResultSetRow.getString("additional_params")).andReturn(additionalParams).times(1);
        expect(mockNullAsZeroResultSetRow.getArray("category_taxomony")).andReturn(catTax).times(1);
        expect(mockNullAsZeroResultSetRow.getString("sie_json")).andReturn(sIEJson).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("impression_ceil")).andReturn(impressionCeil).times(1);
        expect(mockNullAsZeroResultSetRow.getString("manuf_model_targeting")).andReturn(manufModelTargeting).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("ecpm_boost")).andReturn(ecpmBoost).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("boost_date")).andReturn(eCPMBoostDate).anyTimes();
        expect(mockNullAsZeroResultSetRow.getArray("tod")).andReturn(tod).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("dst")).andReturn(dst).times(1);
        expect(mockNullAsZeroResultSetRow.getLong("campaign_inc_id")).andReturn(campaignIncId).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifyTime).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);



        replayAll();

        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        MemberModifier.field(ChannelAdGroupRepository.class, "logger").set(tested, mockLogger);
        final DBEntity<ChannelSegmentEntity, String> entity = tested.buildObjectFromRow(null);
        final ChannelSegmentEntity output = entity.getObject();

        assertThat(output.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(output.getAdgroupId(), is(equalTo(adgroupId)));
        assertThat(output.getAdIds(), is(equalTo(adIds)));
        assertThat(output.getChannelId(), is(equalTo(channelId)));
        assertThat(output.getExternalSiteKey(), is(equalTo(externalSiteKey)));
        assertThat(output.getCampaignId(), is(equalTo(campaignId)));
        assertThat(output.getIncIds(), is(equalTo(adIncIds)));
        assertThat(output.getAdgroupIncId(), is(equalTo(adgroupIncId)));
        assertThat(output.isStatus(), is(equalTo(status)));
        assertThat(output.getPricingModel(), is(equalTo(pricingModel.toUpperCase())));
        assertThat(output.isTestMode(), is(equalTo(isTestMode)));
        assertThat(output.getSiteRatings(), is(equalTo(siteRatings)));
        assertThat(output.getRcList(), is(equalTo(rcList)));
        assertThat(output.getSlotIds(), is(equalTo(slotIds)));
        assertThat(output.getAdFormatIds(), is(equalTo(creativeTypes)));
        assertThat(output.getTags(), is(equalTo(tags)));
        assertThat(output.getPlatformTargeting(), is(equalTo(platformTargeting)));
        assertThat(output.getTargetingPlatform(), is(equalTo(targetingPlatformResult)));
        assertThat(output.isUdIdRequired(), is(equalTo(true)));
        assertThat(output.isLatlongRequired(), is(equalTo(true)));
        assertThat(output.isZipCodeRequired(), is(equalTo(true)));
        assertThat(output.isStripUdId(), is(equalTo(false)));
        assertThat(output.isStripLatlong(), is(equalTo(false)));
        assertThat(output.isStripZipCode(), is(equalTo(false)));
        assertThat(output.isRestrictedToRichMediaOnly(), is(equalTo(false)));
        assertThat(output.isInterstitialOnly(), is(equalTo(false)));
        assertThat(output.isNonInterstitialOnly(), is(equalTo(true)));
        assertThat(output.isAppUrlEnabled(), is(equalTo(true)));

        assertThat(output.isAllTags(), is(equalTo(true)));
        assertThat(output.getCategoryTaxonomy(), is(equalTo(catTaxResult)));
        assertThat(output.getSitesIE(), is(equalTo(sitesIEResult)));
        assertThat(output.isSiteInclusion(), is(equalTo(siteInclusionResult)));
        assertThat(output.getAdditionalParams().toString(), is(equalTo(additionalParamsResult.toString())));
        assertThat(output.getImpressionCeil(), is(equalTo(impressionCeil)));
        assertThat(output.getManufModelTargetingList(), is(equalTo(manufModelTargetingResult)));
        assertThat(output.getOsIds(), is(equalTo(osIdsResult)));
        assertThat(output.getEcpmBoost(), is(equalTo(ecpmBoost)));
        assertThat(output.getTod(), is(equalTo(tod)));
        assertThat(output.getDst(), is(equalTo(dst)));
        assertThat(output.getCampaignIncId(), is(equalTo(campaignIncId)));

        verifyAll();
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }

    @Test
    public void testParseOsIds() throws Exception {
        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        final String osVersionTargeting = "{os:[{id:5,incl:true}]}";
        final List<Integer> osIdsResult = new ArrayList<Integer>(Arrays.asList(5));
        assertThat(tested.parseOsIds(osVersionTargeting), is(equalTo(osIdsResult)));
    }

    @Test
    public void testGetSites() throws Exception {
        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        final String sIEJson = "{\"sites\": [\"93cda0ada1e5427694acb29efc0f29a2\"], \"mode\": \"exclusion\"}";
        final Set<String> sitesIEResult = new HashSet<String>(Arrays.asList("93cda0ada1e5427694acb29efc0f29a2"));
        assertThat(tested.getSites(sIEJson), is(equalTo(sitesIEResult)));
    }

    @Test
    public void testGetMode() throws Exception {
        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        final String sIEJson = "{\"sites\": [\"93cda0ada1e5427694acb29efc0f29a2\"], \"mode\": \"exclusion\"}";
        final boolean siteInclusionResult = false;
        assertThat(tested.getMode(sIEJson), is(equalTo(siteInclusionResult)));
    }

    @Test
    public void testParseManufacturingIds() throws Exception {
        final ChannelAdGroupRepository tested = new ChannelAdGroupRepository();
        final String manufModelTargeting = "{manuf:[{id:16,modelIds:[3],incl:true},{id:5,modelIds:[1],incl:true}]}";
        final List<Integer> manufModelTargetingResult = new ArrayList<Integer>(Arrays.asList(3, 1));
        assertThat(tested.parseManufacturingIds(manufModelTargeting), is(equalTo(manufModelTargetingResult)));
    }
}
