package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.*;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

import java.util.*;


public class MatchSegments {
    private DebugLogger                         logger;
    private static final String                 DEFAULT = "default";
    private RepositoryHelper                    repositoryHelper;
    private SASRequestParameters                sasParams;
    private SegmentAdGroupFeedbackEntity        segmentAdGroupFeedbackEntity;
    private static ChannelAdGroupRepository     channelAdGroupRepository;
    private static ChannelEntity                defaultChannelEntity;
    private static ChannelFeedbackEntity        defaultChannelFeedbackEntity;
    private static ChannelSegmentFeedbackEntity defaultChannelSegmentFeedbackEntity;
    private static ChannelSegmentFeedbackEntity defaultChannelSegmentCitrusLeafFeedbackEntity;

    public static void init(ChannelAdGroupRepository channelAdGroupRepository) {
        Set<String> emptySet = new HashSet<String>();
        Double defaultEcpm = ServletHandler.getServerConfig().getDouble("default.ecpm", 0.1);
        MatchSegments.channelAdGroupRepository = channelAdGroupRepository;

        ChannelEntity.Builder channelEntityBuilder = ChannelEntity.newBuilder();
        channelEntityBuilder.setImpressionCeil(Long.MAX_VALUE);
        channelEntityBuilder.setImpressionFloor(0);
        channelEntityBuilder.setPriority(3);
        channelEntityBuilder.setRequestCap(Long.MAX_VALUE);
        channelEntityBuilder.setSiteInclusion(false);
        channelEntityBuilder.setSitesIE(emptySet);
        MatchSegments.defaultChannelEntity = channelEntityBuilder.build();

        ChannelFeedbackEntity.Builder channelFeedbackEntityBuilder = ChannelFeedbackEntity.newBuilder();
        channelFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelFeedbackEntityBuilder.setBalance(Double.MAX_VALUE);
        MatchSegments.defaultChannelFeedbackEntity = channelFeedbackEntityBuilder.build();

        ChannelSegmentFeedbackEntity.Builder channelSegmentFeedbackEntityBuilder = ChannelSegmentFeedbackEntity
                .newBuilder();
        channelSegmentFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setAdGroupId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setECPM(defaultEcpm);
        channelSegmentFeedbackEntityBuilder.setFillRatio(0.01);
        channelSegmentFeedbackEntityBuilder.setLastHourLatency(400);
        MatchSegments.defaultChannelSegmentFeedbackEntity = channelSegmentFeedbackEntityBuilder.build();

        MatchSegments.defaultChannelSegmentCitrusLeafFeedbackEntity = MatchSegments.defaultChannelSegmentFeedbackEntity;
    }

    public MatchSegments(RepositoryHelper repositoryHelper, SASRequestParameters sasParams, DebugLogger logger) {
        this.repositoryHelper = repositoryHelper;
        this.sasParams = sasParams;
        this.logger = logger;
        this.segmentAdGroupFeedbackEntity = repositoryHelper.querySiteCitrusLeafFeedbackRepository(
            sasParams.getSiteId(), sasParams.getSiteSegmentId(), logger);
    }

    // select channel segment based on specified rules
    public Map<String, HashMap<String, ChannelSegment>> matchSegments(SASRequestParameters sasParams) {
        String slotStr = sasParams.getSlot();
        String countryStr = sasParams.getCountryId();
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
            logger.debug("Requesting Parameters :", "slot:", slotStr, "categories:", sasParams.getCategories(),
                "country:", countryStr, "targetingPlatform:", targetingPlatform, "siteRating:", siteRating, "osId",
                osId);
            long slot = Long.parseLong(slotStr);
            long country = -1;
            if (countryStr != null) {
                country = Long.parseLong(countryStr);
            }
            return (matchSegments(logger, slot, getCategories(), country, targetingPlatform, siteRating, osId));
        }
        catch (NumberFormatException exception) {
            logger.error("Error parsing required arguments " + exception.getMessage());
            return null;
        }
    }

    /**
     * repositoryHelper Method which computes categories according to new category taxonomy and returns the category
     * list (old or new) depending upon the config
     */
    public List<Long> getCategories() {
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

    private Map<String, HashMap<String, ChannelSegment>> matchSegments(DebugLogger logger, long slotId,
            List<Long> categories, long country, Integer targetingPlatform, Integer siteRating, int osId) {
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
                    logger.debug("Found", filteredEntities.size(), "adGroups");
                    allFilteredEntities.addAll(filteredEntities);
                }
            }
        }
        if (!allFilteredEntities.isEmpty()) {
            logger.debug("AdGroups are :");
            for (ChannelSegmentEntity entity : allFilteredEntities) {
                logger.debug("AdGroup :", entity.getAdgroupId());
                insertChannelSegmentToResultSet(result, entity);
            }
        }
        if (result.size() == 0) {
            logger.debug("No matching records for the request - slot:", slotId, "categories:", categories, "country:",
                country, "targetingPlatform", targetingPlatform, "siteRating", siteRating, "osId", osId);
        }
        else {
            logger.debug("Final selected list of adGroups : ");
            printSegments(result, logger);
        }
        return result;
    }

    // Loads entities and updates cache if required.
    private Collection<ChannelSegmentEntity> loadEntities(long slotId, long category, long country,
            Integer targetingPlatform, Integer siteRating, int osId) {
        logger.debug("Loading adgroups for slot:", slotId, "category:", category, "country:", country,
            "targetingPlatform:", targetingPlatform, "siteRating:", siteRating, "osId:", osId);
        return channelAdGroupRepository.getEntities(slotId, category, country, targetingPlatform, siteRating, osId);
    }

    private void insertChannelSegmentToResultSet(Map<String, HashMap<String, ChannelSegment>> result,
            ChannelSegmentEntity channelSegmentEntity) {
        if (Filters.getAdvertiserIdToNameMapping().containsKey(channelSegmentEntity.getAdvertiserId())) {
            InspectorStats.incrementStatCount(
                Filters.getAdvertiserIdToNameMapping().get(channelSegmentEntity.getAdvertiserId()),
                InspectorStrings.totalMatchedSegments);
        }

        ChannelSegment channelSegment = createSegment(channelSegmentEntity);

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

    private ChannelSegment createSegment(ChannelSegmentEntity channelSegmentEntity) {
        ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId());
        ChannelFeedbackEntity channelFeedbackEntity = repositoryHelper
                .queryChannelFeedbackRepository(channelSegmentEntity.getAdvertiserId());
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper
                .queryChannelSegmentFeedbackRepository(channelSegmentEntity.getAdgroupId());
        ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity = null;
        if (channelEntity == null) {
            logger.debug("No channelEntity for found");
            channelEntity = MatchSegments.defaultChannelEntity;
        }

        if (channelFeedbackEntity == null) {
            logger.debug("No channelFeedbackEntity found");
            channelFeedbackEntity = MatchSegments.defaultChannelFeedbackEntity;
        }

        if (channelSegmentFeedbackEntity == null) {
            logger.debug("No channelSegmentFeedbackEntity found");
            channelSegmentFeedbackEntity = MatchSegments.defaultChannelSegmentFeedbackEntity;
        }

        if (segmentAdGroupFeedbackEntity != null) {
            if (segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap() != null)
                channelSegmentCitrusLeafFeedbackEntity = segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap().get(
                    channelSegmentEntity.getExternalSiteKey());
        }
        else {
            logger.debug("No segmentAdGroupFeedbackEntity found");
        }

        if (channelSegmentCitrusLeafFeedbackEntity == null) {
            logger.debug("No channelSegmentCitrusLeafFeedbackEntity");
            channelSegmentCitrusLeafFeedbackEntity = MatchSegments.defaultChannelSegmentCitrusLeafFeedbackEntity;
        }
        double pEcpm = channelSegmentCitrusLeafFeedbackEntity.getECPM();
        return new ChannelSegment(channelSegmentEntity, channelEntity, channelFeedbackEntity,
                channelSegmentFeedbackEntity, channelSegmentCitrusLeafFeedbackEntity, null, pEcpm);
    }

    public static void printSegments(Map<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger) {
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
                Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
                for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                    ChannelSegment channelSegment = adGroupEntry.getValue();
                    logger.debug("Advertiser :", channelSegment.getChannelSegmentEntity().getAdvertiserId(),
                        ", AdGroup :", channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }
}
