package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class ChannelAdGroupRepository extends AbstractStatsMaintainingDBRepository<ChannelSegmentEntity, String>
        implements RepositoryManager {

    @Override
    public DBEntity<ChannelSegmentEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        String adgroupId = row.getString("adgroup_id");
        Timestamp modifyTime = row.getTimestamp("modified_on");
        try {
            String advertiserId = row.getString("advertiser_id");
            String[] adIds = (String[]) row.getArray("ad_ids");
            String channelId = row.getString("channel_id");
            String externalSiteKey = row.getString("external_site_key");
            String campaignId = row.getString("campaign_id");
            Long[] adIncIds = (Long[]) row.getArray("ad_inc_ids");
            long adgroupIncId = row.getLong("adgroup_inc_id");
            boolean status = row.getBoolean("status");
            String pricingModel = row.getString("pricing_model");
            boolean isTestMode = row.getBoolean("is_test_mode");
            Timestamp modified_on = row.getTimestamp("modified_on");
            Integer[] siteRatings = (Integer[]) row.getArray("site_ratings");
            Long[] rcList = (Long[]) row.getArray("rc_list");
            Long[] slotIds = (Long[]) row.getArray("slot_ids");
            Integer[] creativeTypes = (Integer[]) row.getArray("creative_types");
            Long[] tags = null;
            List<Integer> segmentFlags;
            if (null != row.getArray("segment_flags")) {
                segmentFlags = Arrays.asList((Integer[]) row.getArray("segment_flags"));
            }
            else {
                segmentFlags = new ArrayList<Integer>();
            }
            HashSet<Integer> segmentFlagSet = new HashSet<Integer>();
            segmentFlagSet.addAll(segmentFlags);
            long platformTargeting = row.getLong("platform_targeting_int");
            int targetingPlatform = row.getInt("targeting_platform");
            String osVersionTargeting = row.getString("os_version_targeting");
            String additionalParams = row.getString("additional_params");
            ArrayList<Integer> osIds = parseOsIds(osVersionTargeting);
            boolean udIdRequired = segmentFlagSet.contains(1);
            boolean latlongRequired = segmentFlagSet.contains(2);
            boolean zipCodeRequired = segmentFlagSet.contains(3);
            boolean richMediaOnly = segmentFlagSet.contains(7);
            boolean appUrlEnabled = segmentFlagSet.contains(10);
            boolean stripUdId = segmentFlagSet.contains(4);
            boolean stripZipCode = segmentFlagSet.contains(6);
            boolean stripLatlong = segmentFlagSet.contains(5);
            boolean interstitialOnly = segmentFlagSet.contains(8);
            boolean nonInterstitialOnly = segmentFlagSet.contains(9);
            boolean allTags = false;
            Integer[] catTax = (Integer[]) row.getArray("category_taxomony");
            Long[] categoryTaxomony;
            if (null == catTax || catTax.length == 0) {
                allTags = true;
                categoryTaxomony = new Long[0];
            }
            else {
                categoryTaxomony = new Long[catTax.length];
                for (int i = 0; i < catTax.length; i++) {
                    categoryTaxomony[i] = Long.valueOf(catTax[i]);
                }
            }
            String sIEJson = row.getString("sie_json");
            Set<String> sitesIE = getSites(sIEJson);
            boolean isSiteIncl = getMode(sIEJson);
            long impressionCeil = row.getLong("impression_ceil");
            if (impressionCeil == 0) {
                impressionCeil = Integer.MAX_VALUE;
            }
            String manufModelTargeting = row.getString("manuf_model_targeting");
            ArrayList<Integer> manufModelTargetingList = parseManufacturingIds(manufModelTargeting);
            double ecpmBoost = row.getDouble("ecpm_boost");
            Timestamp eCPMBoostDate = row.getTimestamp("boost_date");
            long millisInDay = 24 * 60 * 60 * 1000;
            Date eCPMBoostExpiryDate = null;
            if (eCPMBoostDate != null) {
                eCPMBoostExpiryDate = new Date(((eCPMBoostDate.getTime() + 3 * millisInDay) / millisInDay)
                        * millisInDay);
            }
            Long[] tod = (Long[]) row.getArray("tod");
            int dst = row.getInt("dst");
            long campaignIncId = row.getLong("campaign_inc_id");

            ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
            builder.setAdvertiserId(advertiserId);
            builder.setAdgroupId(adgroupId);
            builder.setAdIds(adIds);
            builder.setChannelId(channelId);
            builder.setPlatformTargeting(platformTargeting);
            builder.setAdgroupIncId(adgroupIncId);
            builder.setRcList(rcList);
            builder.setTags(tags);
            builder.setAllTags(allTags);
            builder.setStatus(status);
            builder.setTestMode(isTestMode);
            builder.setExternalSiteKey(externalSiteKey);
            builder.setModified_on(modified_on);
            builder.setCampaignId(campaignId);
            builder.setSlotIds(slotIds);
            builder.setIncIds(adIncIds);
            builder.setPricingModel(pricingModel.toUpperCase());
            builder.setSiteRatings(siteRatings);
            builder.setTargetingPlatform(targetingPlatform);
            builder.setOsIds(osIds);
            builder.setUdIdRequired(udIdRequired);
            builder.setLatlongRequired(latlongRequired);
            builder.setStripZipCode(zipCodeRequired);
            builder.setRestrictedToRichMediaOnly(richMediaOnly);
            builder.setAppUrlEnabled(appUrlEnabled);
            builder.setInterstitialOnly(interstitialOnly);
            builder.setNonInterstitialOnly(nonInterstitialOnly);
            builder.setStripUdId(stripUdId);
            builder.setStripLatlong(stripLatlong);
            builder.setStripZipCode(stripZipCode);
            builder.setAdditionalParams(getJSONFromString(additionalParams));
            builder.setCategoryTaxonomy(categoryTaxomony);
            builder.setSitesIE(sitesIE);
            builder.setSiteInclusion(isSiteIncl);
            builder.setImpressionCeil(impressionCeil);
            builder.setManufModelTargetingList(manufModelTargetingList);
            builder.setEcpmBoost(ecpmBoost);
            builder.setEcpmBoostExpiryDate(eCPMBoostExpiryDate);
            builder.setTod(tod);
            builder.setDst(dst);
            builder.setCampaignIncId(campaignIncId);
            builder.setAdFormatIds(creativeTypes);

            ChannelSegmentEntity entity = builder.build();

            logger.debug("Adding adgroup " + adgroupId + " to channel segment repository");
            return new DBEntity<ChannelSegmentEntity, String>(entity, modifyTime);
        }
        catch (Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<ChannelSegmentEntity, String>(new EntityError<String>(adgroupId,
                    "ERROR_IN_EXTRACTING_SEGMENT"), modifyTime);
        }
    }

    JSONObject getJSONFromString(final String additionalParams) {
        if (additionalParams != null) {
            try {
                return new JSONObject(additionalParams);
            }
            catch (JSONException e) {
                logger.info("Error in parsing aditional params json");
            }
        }
        return new JSONObject();
    }

    // Made protected for testing visibility.
    ArrayList<Integer> parseOsIds(final String osVersionTargeting) {
        ArrayList<Integer> osIds = null;
        try {
            if (osVersionTargeting != null) {
                JSONArray osIdsJson = new JSONObject(osVersionTargeting).getJSONArray("os");
                osIds = new ArrayList<Integer>(osIdsJson.length());
                for (int index = 0; index < osIdsJson.length(); ++index) {
                    osIds.add(osIdsJson.getJSONObject(index).getInt("id"));
                }
            }
        }
        catch (JSONException e) {
            // Do Nothing
        }
        return osIds;
    }

    // Made protected for testing visibility.
    public ArrayList<Integer> parseManufacturingIds(final String manufModelTargeting) {
        ArrayList<Integer> modelIds = null;
        try {
            if (manufModelTargeting != null) {
                JSONArray modelIdsJson = new JSONObject(manufModelTargeting).getJSONArray("manuf");
                modelIds = new ArrayList<Integer>(modelIdsJson.length());
                for (int index = 0; index < modelIdsJson.length(); ++index) {
                    JSONArray jsonArray = modelIdsJson.getJSONObject(index).getJSONArray("modelIds");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        modelIds.add(jsonArray.getInt(i));
                    }
                }
            }
        }
        catch (JSONException e) {
            // Do Nothing
        }
        return modelIds;
    }

    boolean getMode(final String sIEJson) {
        boolean mode = false;
        if (sIEJson != null) {
            try {
                JSONObject jObject = new JSONObject(sIEJson);
                mode = "inclusion".equals(jObject.getString("mode"));
            }
            catch (JSONException e) {
                logger.info("wrong json in site_json in channel repo" + e);
            }
        }
        return mode;
    }

    Set<String> getSites(final String sIEJson) {
        Set<String> sitesIE = new HashSet<String>();
        if (sIEJson != null) {
            try {
                JSONObject jObject = new JSONObject(sIEJson);
                JSONArray sites = jObject.getJSONArray("sites");
                for (int i = 0; i < sites.length(); i++) {
                    sitesIE.add(sites.getString(i));
                }
            }
            catch (JSONException e) {
                logger.info("wrong json in site_json in channel repo" + e);
            }
        }
        return sitesIE;
    }

    @Override
    public boolean isObjectToBeDeleted(final ChannelSegmentEntity entity) {
        List<String> matchingKeys;
        try {
            ChannelSegmentEntity oldEntity = query(entity.getId());
            if (oldEntity != null) {
                matchingKeys = ChannelSegmentMatchingCache.generateMatchingKeys(oldEntity);
                ChannelSegmentMatchingCache.cleanupEntityFromCache(oldEntity, matchingKeys);
            }
        }
        catch (RepositoryException e) {
            logger.error("Error in cleaning entity in ChannelAdGroupRepository for id" + entity.getId());
        }
        if (entity.isStatus() && entity.getSiteRatings() != null && entity.getSlotIds() != null) {
            matchingKeys = ChannelSegmentMatchingCache.generateMatchingKeys(entity);
            ChannelSegmentMatchingCache.insertEntityToCache(entity, matchingKeys);
            return false;
        }
        else {
            return true;
        }

    }

    @Override
    public HashIndexKeyBuilder<ChannelSegmentEntity> getHashIndexKeyBuilder(final String arg0) {
        return null;
    }

    @Override
    public ChannelSegmentEntity queryUniqueResult(final RepositoryQuery arg0) throws RepositoryException {
        return null;
    }

    public Collection<ChannelSegmentEntity> getEntities(final long slotId, final long category, final long country,
            final Integer targetingPlatform, final Integer siteRating, final Integer osId) {
        return ChannelSegmentMatchingCache.getEntities(slotId, category, country, targetingPlatform, siteRating, osId);
    }

}
