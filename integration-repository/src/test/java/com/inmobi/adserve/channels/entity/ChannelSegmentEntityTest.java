package com.inmobi.adserve.channels.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.casthrift.ADCreativeType;

@RunWith(PowerMockRunner.class)
public class ChannelSegmentEntityTest {

    public ChannelSegmentEntity.Builder getChannelSegmentEntityBuilder(final String advertiserId,
            final String adgroupId, final String[] adIds, final String channelId, final long platformTargeting,
            final Long[] rcList, final Long[] tags, final boolean status, final boolean isTestMode,
            final String externalSiteKey, final Timestamp modified_on, final String campaignId, final Long[] slotIds,
            final Long[] incIds, final boolean allTags, final String pricingModel, final Integer[] siteRatings,
            final int targetingPlatform, final ArrayList<Integer> osIds, final boolean udIdRequired,
            final boolean zipCodeRequired, final boolean latlongRequired, final boolean richMediaOnly,
            final boolean appUrlEnabled, final boolean interstitialOnly, final boolean nonInterstitialOnly,
            final boolean stripUdId, final boolean stripZipCode, final boolean stripLatlong,
            final JSONObject additionalParams, final List<Integer> manufModelTargetingList, final double ecpmBoost,
            final Date eCPMBoostDate, final Long[] tod, final long adGroupIncId, final Integer[] AdFormatIds) {
        final ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setAdvertiserId(advertiserId);
        builder.setAdgroupId(adgroupId);
        builder.setAdIds(adIds);
        builder.setChannelId(channelId);
        builder.setPlatformTargeting(platformTargeting);
        builder.setRcList(rcList);
        builder.setTags(tags);
        builder.setCategoryTaxonomy(tags);
        builder.setAllTags(allTags);
        builder.setStatus(status);
        builder.setTestMode(isTestMode);
        builder.setExternalSiteKey(externalSiteKey);
        builder.setModified_on(modified_on);
        builder.setCampaignId(campaignId);
        builder.setSlotIds(slotIds);
        builder.setIncIds(incIds);
        builder.setAdgroupIncId(incIds[0]);
        builder.setPricingModel(pricingModel);
        builder.setSiteRatings(siteRatings);
        builder.setTargetingPlatform(targetingPlatform);
        builder.setOsIds(osIds);
        builder.setUdIdRequired(udIdRequired);
        builder.setLatlongRequired(latlongRequired);
        builder.setZipCodeRequired(zipCodeRequired);
        builder.setRestrictedToRichMediaOnly(richMediaOnly);
        builder.setAppUrlEnabled(appUrlEnabled);
        builder.setInterstitialOnly(interstitialOnly);
        builder.setNonInterstitialOnly(nonInterstitialOnly);
        builder.setStripUdId(stripUdId);
        builder.setStripLatlong(stripLatlong);
        builder.setStripZipCode(stripZipCode);
        builder.setAdditionalParams(additionalParams);
        builder.setManufModelTargetingList(manufModelTargetingList);
        builder.setEcpmBoost(ecpmBoost);
        builder.setEcpmBoostExpiryDate(eCPMBoostDate);
        builder.setTod(tod);
        builder.setAdgroupIncId(adGroupIncId);
        builder.setAdFormatIds(AdFormatIds);
        return builder;
    }

    @Test
    public void testChannelSegmentEntity() throws Exception {
        final String advertiserId = "AdvertiserId";
        final String adgroupId = "AdGroupId";
        final String[] adIds = {"AdId1", "AdId2", "AdId3"};
        final String channelId = "ChannelId";
        final long platformTargeting = 3L;
        final Long[] rcList = {4L, 5L};
        final Long[] tags = {6L};
        final boolean status = true;
        final boolean isTestMode = false;
        final String externalSiteKey = "ExternalSiteKey";
        final Timestamp modified_on = new Timestamp(15L);
        final String campaignId = "CampaignId";
        final Long[] slotIds = {9L, 15L};
        final Long[] incIds = {21L, 25L, 35L};
        final boolean allTags = true;
        final String pricingModel = "PricingModel";
        final Integer[] siteRatings = {25, 26};
        final int targetingPlatform = 2;
        final ArrayList<Integer> osIds = new ArrayList<>();
        final boolean udIdRequired = true;
        final boolean zipCodeRequired = true;
        final boolean latlongRequired = false;
        final boolean richMediaOnly = true;
        final boolean appUrlEnabled = true;
        final boolean interstitialOnly = false;
        final boolean nonInterstitialOnly = false;
        final boolean stripUdId = true;
        final boolean stripZipCode = true;
        final boolean stripLatlong = false;
        final JSONObject additionalParams = null;
        final List<Integer> manufModelTargetingList = Arrays.asList(4, 5);
        final double ecpmBoost = 0.5;
        final Date eCPMBoostDate = new Date();
        final Long[] tod = {11L, 12L};
        final long adGroupIncId = 65L;
        final Integer[] adFormatIds = {9, 11, 0, -1};

        final ChannelSegmentEntity tested =
                getChannelSegmentEntityBuilder(advertiserId, adgroupId, adIds, channelId, platformTargeting, rcList,
                        tags, status, isTestMode, externalSiteKey, modified_on, campaignId, slotIds, incIds, allTags,
                        pricingModel, siteRatings, targetingPlatform, osIds, udIdRequired, zipCodeRequired,
                        latlongRequired, richMediaOnly, appUrlEnabled, interstitialOnly, nonInterstitialOnly,
                        stripUdId, stripZipCode, stripLatlong, additionalParams, manufModelTargetingList, ecpmBoost,
                        eCPMBoostDate, tod, adGroupIncId, adFormatIds).build();

        assertThat(tested.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(tested.getAdgroupId(), is(equalTo(adgroupId)));
        assertThat(tested.getAdIds(), is(equalTo(adIds)));
        assertThat(tested.getChannelId(), is(equalTo(channelId)));
        assertThat(tested.getRcList(), is(equalTo(rcList)));
        assertThat(tested.getTags(), is(equalTo(tags)));
        assertThat(tested.isStatus(), is(equalTo(status)));
        assertThat(tested.isTestMode(), is(equalTo(isTestMode)));
        assertThat(tested.getExternalSiteKey(), is(equalTo(externalSiteKey)));
        assertThat(tested.getModified_on(), is(equalTo(modified_on)));
        assertThat(tested.getCampaignId(), is(equalTo(campaignId)));
        assertThat(tested.getSlotIds(), is(equalTo(slotIds)));
        assertThat(tested.getIncIds(), is(equalTo(incIds)));
        assertThat(tested.isAllTags(), is(equalTo(allTags)));
        assertThat(tested.getPricingModel(), is(equalTo(pricingModel)));
        assertThat(tested.getSiteRatings(), is(equalTo(siteRatings)));

        final ArrayList<Integer> targetingPlatformList = new ArrayList<>();
        targetingPlatformList.add(targetingPlatform);
        assertThat(tested.getTargetingPlatform(), is(equalTo(targetingPlatformList)));
        // assertThat(tested.getOsIds(), is(equalTo(osIds)));
        assertThat(tested.isUdIdRequired(), is(equalTo(udIdRequired)));
        assertThat(tested.isZipCodeRequired(), is(equalTo(zipCodeRequired)));
        assertThat(tested.isLatlongRequired(), is(equalTo(latlongRequired)));
        assertThat(tested.getPlatformTargeting(), is(equalTo(platformTargeting)));
        assertThat(tested.isRestrictedToRichMediaOnly(), is(equalTo(richMediaOnly)));
        assertThat(tested.isAppUrlEnabled(), is(equalTo(appUrlEnabled)));
        assertThat(tested.isInterstitialOnly(), is(equalTo(interstitialOnly)));
        assertThat(tested.isNonInterstitialOnly(), is(equalTo(nonInterstitialOnly)));
        assertThat(tested.isStripUdId(), is(equalTo(stripUdId)));
        assertThat(tested.isStripLatlong(), is(equalTo(stripLatlong)));
        assertThat(tested.getAdditionalParams(), is(equalTo(additionalParams)));
        assertThat(tested.getManufModelTargetingList(), is(equalTo(manufModelTargetingList)));
        assertThat(tested.getEcpmBoost(), is(equalTo(ecpmBoost)));
        assertThat(tested.getEcpmBoostExpiryDate(), is(equalTo(eCPMBoostDate)));
        assertThat(tested.getTod(), is(equalTo(tod)));
        assertThat(tested.getAdgroupIncId(), is(equalTo(adGroupIncId)));
        assertThat(tested.getAdFormatIds(), is(equalTo(adFormatIds)));

        assertThat(tested.getId(), is(equalTo(adgroupId)));
        assertThat(tested.getJSON(), is(equalTo(null)));
        assertThat(tested.getIncId(ADCreativeType.NATIVE), is(equalTo(incIds[0])));
        assertThat(tested.getIncId(ADCreativeType.INTERSTITIAL_VIDEO), is(equalTo(incIds[1])));
        assertThat(tested.getIncId(ADCreativeType.BANNER), is(equalTo(incIds[2])));
        assertThat(tested.getIncId(ADCreativeType.findByValue(-1)), is(equalTo(-1L)));
        assertThat(tested.getAdId(ADCreativeType.NATIVE), is(equalTo(adIds[0])));
        assertThat(tested.getAdId(ADCreativeType.INTERSTITIAL_VIDEO), is(equalTo(adIds[1])));
        assertThat(tested.getAdId(ADCreativeType.BANNER), is(equalTo(adIds[2])));
        assertThat(tested.getAdId(ADCreativeType.findByValue(-1)), is(equalTo("")));
    }
}
