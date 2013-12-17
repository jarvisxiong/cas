package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
public class MatchSegments {
    private final String                       DEFAULT = "default";
    private final RepositoryHelper             repositoryHelper;
    private final ChannelAdGroupRepository     channelAdGroupRepository;
    private final ChannelEntity                defaultChannelEntity;
    private final ChannelFeedbackEntity        defaultChannelFeedbackEntity;
    private final ChannelSegmentFeedbackEntity defaultChannelSegmentFeedbackEntity;
    private final ChannelSegmentFeedbackEntity defaultChannelSegmentCitrusLeafFeedbackEntity;
    private static final Logger                LOG     = LoggerFactory.getLogger(MatchSegments.class);

    @Inject
    public MatchSegments(final RepositoryHelper repositoryHelper) {
        this.repositoryHelper = repositoryHelper;

        Double defaultEcpm = ServletHandler.getServerConfig().getDouble("default.ecpm", 0.1);
        channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();

        ChannelEntity.Builder channelEntityBuilder = ChannelEntity.newBuilder();
        channelEntityBuilder.setImpressionCeil(Long.MAX_VALUE);
        channelEntityBuilder.setImpressionFloor(0);
        channelEntityBuilder.setPriority(3);
        channelEntityBuilder.setRequestCap(Long.MAX_VALUE);
        channelEntityBuilder.setSiteInclusion(false);
        channelEntityBuilder.setSitesIE(new HashSet<String>());
        defaultChannelEntity = channelEntityBuilder.build();

        ChannelFeedbackEntity.Builder channelFeedbackEntityBuilder = ChannelFeedbackEntity.newBuilder();
        channelFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelFeedbackEntityBuilder.setBalance(Double.MAX_VALUE);
        defaultChannelFeedbackEntity = channelFeedbackEntityBuilder.build();

        ChannelSegmentFeedbackEntity.Builder channelSegmentFeedbackEntityBuilder = ChannelSegmentFeedbackEntity
                .newBuilder();
        channelSegmentFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setAdGroupId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setECPM(defaultEcpm);
        channelSegmentFeedbackEntityBuilder.setFillRatio(0.01);
        channelSegmentFeedbackEntityBuilder.setLastHourLatency(400);
        defaultChannelSegmentFeedbackEntity = channelSegmentFeedbackEntityBuilder.build();

        this.defaultChannelSegmentCitrusLeafFeedbackEntity = this.defaultChannelSegmentFeedbackEntity;
    }

    // select channel segment based on specified rules
    public Map<String, HashMap<String, ChannelSegment>> matchSegments(final SASRequestParameters sasParams) {
        String slotStr = sasParams.getSlot();
        String countryStr = sasParams.getCountryStr();
        int osId = sasParams.getOsId();
        String sourceStr = sasParams.getSource();
        String siteRatingStr = sasParams.getSiteType();
        Integer targetingPlatform = (sourceStr == null || sourceStr.equalsIgnoreCase("wap")) ? 2 : 1 /* app */;
        Integer siteRating = -1;
        if (null == siteRatingStr || slotStr == null || sasParams.getCategories() == null
                || sasParams.getCategories().isEmpty()) {
            return null;
        }
        if (siteRatingStr.equalsIgnoreCase("performance")) {
            siteRating = 0;
        }
        else if (siteRatingStr.equalsIgnoreCase("mature")) {
            siteRating = 1;
        }
        else if (siteRatingStr.equalsIgnoreCase("family_safe")) {
            siteRating = 2;
        }
        try {
            LOG
                    .debug(
                        "Requesting Parameters :  slot: {} categories: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
                        slotStr, sasParams.getCategories(), countryStr, targetingPlatform, siteRating, osId);
            long slot = Long.parseLong(slotStr);
            long country = -1;
            if (countryStr != null) {
                country = Long.parseLong(countryStr);
            }

            return (matchSegments(slot, getCategories(sasParams), country, targetingPlatform, siteRating, osId,
                sasParams));
        }
        catch (NumberFormatException exception) {
            LOG.error("Error parsing required arguments {}", exception);
            return null;
        }
    }

    /**
     * repositoryHelper Method which computes categories according to new category taxonomy and returns the category
     * list (old or new) depending upon the config
     */
    List<Long> getCategories(final SASRequestParameters sasParams) {
        // Computing all the parents for categories in the category list from the
        // request
        HashSet<Long> categories = new HashSet<Long>();
        if (null != sasParams.getCategories()) {
            for (Long cat : sasParams.getCategories()) {
                String parentId = cat.toString();
                while (parentId != null) {
                    categories.add(Long.parseLong(parentId));
                    SiteTaxonomyEntity entity = repositoryHelper.querySiteTaxonomyRepository(parentId);
                    if (entity == null) {
                        break;
                    }
                    parentId = entity.getParentId();
                }
            }
        }
        // setting Categories field in sasParams to contain their parentids as well
        List<Long> temp = new ArrayList<Long>();
        temp.addAll(categories);
        sasParams.setCategories(temp);
        return sasParams.getCategories();
    }

    private Map<String, HashMap<String, ChannelSegment>> matchSegments(final long slotId, final List<Long> categories,
            final long country, final Integer targetingPlatform, final Integer siteRating, final int osId,
            final SASRequestParameters sasParams) {
        Map<String, HashMap<String, ChannelSegment>> result = new HashMap<String, HashMap<String, ChannelSegment>>();
        Set<ChannelSegmentEntity> allFilteredEntities = new HashSet<ChannelSegmentEntity>();

        // adding -1 for all categories
        categories.add(-1l);
        // adding -1 for all countries
        long[] countries = { -1 };
        if (country != -1) {
            countries = new long[] { -1, country };
        }
        // adding -1 for all osIds
        int[] osIds = new int[] { -1, osId };

        for (long category : categories) {
            for (long countryId : countries) {
                for (int os : osIds) {
                    Collection<ChannelSegmentEntity> filteredEntities = loadEntities(slotId, category, countryId,
                        targetingPlatform, siteRating, os);
                    LOG.debug("Found {} adGroups", filteredEntities.size());
                    allFilteredEntities.addAll(filteredEntities);
                }
            }
        }
        if (!allFilteredEntities.isEmpty()) {
            LOG.debug("AdGroups are :");
            for (ChannelSegmentEntity entity : allFilteredEntities) {
                LOG.debug("AdGroup : {}", entity.getAdgroupId());
                insertChannelSegmentToResultSet(result, entity, sasParams);
            }
        }
        if (result.size() == 0) {
            LOG
                    .debug(
                        "No matching records for the request - slot: {} categories: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
                        slotId, categories, country, targetingPlatform, siteRating, osId);
        }
        else {
            LOG.debug("Final selected list of adGroups : ");
            printSegments(result);
        }
        return result;
    }

    // Loads entities and updates cache if required.
    private Collection<ChannelSegmentEntity> loadEntities(final long slotId, final long category, final long country,
            final Integer targetingPlatform, final Integer siteRating, final int osId) {
        LOG.debug(
            "Loading adgroups for slot: {} category: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
            slotId, category, country, targetingPlatform, siteRating, osId);
        return channelAdGroupRepository.getEntities(slotId, category, country, targetingPlatform, siteRating, osId);
    }

    private void insertChannelSegmentToResultSet(final Map<String, HashMap<String, ChannelSegment>> result,
            final ChannelSegmentEntity channelSegmentEntity, final SASRequestParameters sasParams) {
        if (Filters.getAdvertiserIdToNameMapping().containsKey(channelSegmentEntity.getAdvertiserId())) {
            InspectorStats.incrementStatCount(
                Filters.getAdvertiserIdToNameMapping().get(channelSegmentEntity.getAdvertiserId()),
                InspectorStrings.totalMatchedSegments);
        }

        ChannelSegment channelSegment = createSegment(channelSegmentEntity, sasParams);

        if (result.get(channelSegmentEntity.getAdvertiserId()) == null) {
            HashMap<String, ChannelSegment> hashMap = new HashMap<String, ChannelSegment>();
            hashMap.put(channelSegmentEntity.getAdgroupId(), channelSegment);
            result.put(channelSegmentEntity.getAdvertiserId(), hashMap);
        }
        else {
            HashMap<String, ChannelSegment> hashMap = result.get(channelSegmentEntity.getAdvertiserId());
            hashMap.put(channelSegmentEntity.getAdgroupId(), channelSegment);
            result.put(channelSegmentEntity.getAdvertiserId(), hashMap);
        }

    }

    private ChannelSegment createSegment(final ChannelSegmentEntity channelSegmentEntity,
            final SASRequestParameters sasParams) {
        ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId());
        ChannelFeedbackEntity channelFeedbackEntity = repositoryHelper
                .queryChannelFeedbackRepository(channelSegmentEntity.getAdvertiserId());
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper
                .queryChannelSegmentFeedbackRepository(channelSegmentEntity.getAdgroupId());
        ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity = null;
        if (channelEntity == null) {
            LOG.debug("No channelEntity for found");
            channelEntity = defaultChannelEntity;
        }

        if (channelFeedbackEntity == null) {
            LOG.debug("No channelFeedbackEntity found");
            channelFeedbackEntity = defaultChannelFeedbackEntity;
        }

        if (channelSegmentFeedbackEntity == null) {
            LOG.debug("No channelSegmentFeedbackEntity found");
            channelSegmentFeedbackEntity = defaultChannelSegmentFeedbackEntity;
        }

        SegmentAdGroupFeedbackEntity segmentAdGroupFeedbackEntity = repositoryHelper
                .querySiteCitrusLeafFeedbackRepository(sasParams.getSiteId(), sasParams.getSiteSegmentId());

        if (segmentAdGroupFeedbackEntity != null) {
            if (segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap() != null) {
                channelSegmentCitrusLeafFeedbackEntity = segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap().get(
                    channelSegmentEntity.getExternalSiteKey());
            }
        }
        else {
            LOG.debug("No segmentAdGroupFeedbackEntity found");
        }

        if (channelSegmentCitrusLeafFeedbackEntity == null) {
            LOG.debug("No channelSegmentCitrusLeafFeedbackEntity");
            channelSegmentCitrusLeafFeedbackEntity = defaultChannelSegmentCitrusLeafFeedbackEntity;
        }

        double pEcpm = channelSegmentCitrusLeafFeedbackEntity.getECPM();
        return new ChannelSegment(channelSegmentEntity, channelEntity, channelFeedbackEntity,
                channelSegmentFeedbackEntity, channelSegmentCitrusLeafFeedbackEntity, null, pEcpm);
    }

    private static void printSegments(final Map<String, HashMap<String, ChannelSegment>> matchedSegments) {
        if (LOG.isDebugEnabled()) {
            for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
                Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
                for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                    ChannelSegment channelSegment = adGroupEntry.getValue();
                    LOG.debug("Advertiser :{} , AdGroup : {}", channelSegment
                            .getChannelSegmentEntity()
                                .getAdvertiserId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }
}
