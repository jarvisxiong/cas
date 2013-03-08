package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class MatchSegments {
  private DebugLogger logger;
  private RepositoryHelper repositoryHelper;
  private SASRequestParameters sasParams;
  private static ChannelAdGroupRepository channelAdGroupRepository;
  private static ChannelEntity defaultChannelEntity;
  private static ChannelFeedbackEntity defaultChannelFeedbackEntity;
  private static ChannelSegmentFeedbackEntity defaultChannelSegmentFeedbackEntity;
  private static ChannelSegmentFeedbackEntity defaultChannelSegmentCitrusLeafFeedbackEntity;

  public static void init(ChannelAdGroupRepository channelAdGroupRepository) {
    Set<String> emptySet = new HashSet<String>();
    MatchSegments.channelAdGroupRepository = channelAdGroupRepository;
    MatchSegments.defaultChannelEntity = (new ChannelEntity()).setImpressionCeil(Long.MAX_VALUE).setImpressionFloor(0)
        .setPriority(3).setRequestCap(Long.MAX_VALUE);
    MatchSegments.defaultChannelEntity.setSiteInclusion(false);
    MatchSegments.defaultChannelEntity.setSitesIE(emptySet);
    MatchSegments.defaultChannelFeedbackEntity = new ChannelFeedbackEntity("default", 0, 0, Double.MAX_VALUE, 0, 0, 0,
        0, 0);
    MatchSegments.defaultChannelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity("default", "default", 1.0,
        0.5, 0, 0, 0, 0);
    MatchSegments.defaultChannelSegmentCitrusLeafFeedbackEntity = new ChannelSegmentFeedbackEntity("default",
        "default", 1.0, 0.5, 0, 0, 0, 0);
  }

  public MatchSegments(RepositoryHelper repositoryHelper, SASRequestParameters sasParams, DebugLogger logger) {
    this.repositoryHelper = repositoryHelper;
    this.sasParams = sasParams;
    this.logger = logger;
  }

  // select channel segment based on specified rules
  public Map<String, HashMap<String, ChannelSegment>> matchSegments(SASRequestParameters sasParams) {
    String slotStr = sasParams.getSlot();
    String countryStr = sasParams.getCountryStr();
    int osId = sasParams.getOsId();
    String sourceStr = sasParams.getSource();
    String siteRatingStr = sasParams.getSiteType();
    Integer targetingPlatform = (sourceStr == null || sourceStr.equalsIgnoreCase("wap")) ? 2 : 1 /* app */;
    Integer siteRating = -1;
    if(null == siteRatingStr) {
      return null;
    }
    if(siteRatingStr.equalsIgnoreCase("performance")) {
      siteRating = 0;
    } else if(siteRatingStr.equalsIgnoreCase("mature")) {
      siteRating = 1;
    } else if(siteRatingStr.equalsIgnoreCase("family_safe")) {
      siteRating = 2;
    }
    if(slotStr == null || sasParams.getCategories() == null || sasParams.getCategories().isEmpty()) {
      return null;
    }
    try {
      if(logger.isDebugEnabled()) {
        logger.debug("Request# slot: " + slotStr + " country: " + countryStr + " categories: "
            + sasParams.getCategories() + " targetingPlatform: " + targetingPlatform + " siteRating: " + siteRating
            + " osId" + osId);
      }
      long slot = Long.parseLong(slotStr);
      long country = -1;
      if(countryStr != null) {
        country = Long.parseLong(countryStr);
      }
      return (matchSegments(logger, slot, getCategories(ServletHandler.config), country, targetingPlatform, siteRating,
          osId));
    } catch (NumberFormatException exception) {
      logger.error("Error parsing required arguments " + exception.getMessage());
      return null;
    }
  }

  /**
   * repositoryHelper Method which computes categories according to new category
   * taxonomy and returns the category list (old or new) depending upon the
   * config
   * 
   * @param sasParams
   * @return
   */
  public List<Long> getCategories(Configuration serverConfig) {
    // Computing all the parents for categories in the new category list from
    // the request
    HashSet<Long> newCategories = new HashSet<Long>();
    for (Long cat : sasParams.getNewCategories()) {
      String parentId = cat.toString();
      while (parentId != null) {
        newCategories.add(Long.parseLong(parentId));
        SiteTaxonomyEntity entity = repositoryHelper.querySiteTaxonomyRepository(parentId);
        if(entity == null) {
          break;
        }
        parentId = entity.getParentId();
      }
    }
    // setting newCategories field in sasParams to contain their parentids as
    // well
    List<Long> temp = new ArrayList<Long>();
    temp.addAll(newCategories);
    sasParams.setNewCategories(temp);
    if(serverConfig.getBoolean("isNewCategory", false)) {
      return sasParams.getNewCategories();
    }
    return sasParams.getCategories();
  }

  private Map<String, HashMap<String, ChannelSegment>> matchSegments(DebugLogger logger, long slotId,
      List<Long> categories, long country, Integer targetingPlatform, Integer siteRating, int osId) {
    HashMap<String /* advertiserId */, HashMap<String /* adGroupId */, ChannelSegment>> result = new HashMap<String /* advertiserId */, HashMap<String /* adGroupId */, ChannelSegment>>();

    List<ChannelSegmentEntity> filteredAllCategoriesEntities = loadEntities(slotId, -1, country, targetingPlatform,
        siteRating, osId);

    // Makes sure that there is exactly one entry from each Advertiser.
    for (ChannelSegmentEntity entity : filteredAllCategoriesEntities) {
      if(entity.getStatus()) {
        insertChannelSegmentToResultSet(result, entity);
      }
      logger.debug("AdGroup Dropped due to status - Id:", entity.getAdgroupId());
    }
    logger.debug("Number of entries from all categories in result:",result.size(), result);

    if(country != -1) {
      // Load Data for all countries
      List<ChannelSegmentEntity> allCategoriesAllCountryEntities = loadEntities(slotId, -1, -1, targetingPlatform,
          siteRating, osId);

      // Makes sure that there is exactly one entry from each Advertiser for all
      // countries.
      for (ChannelSegmentEntity entity : allCategoriesAllCountryEntities) {
        if(entity.getStatus()) {
          insertChannelSegmentToResultSet(result, entity);
        } 
        logger.debug("AdGroup Dropped due to status - Id:", entity.getAdgroupId());
      }
        logger.debug("Number of entries from all countries and categories in result:", result.size(), result);
    }

    // Does OR for the categories.
    for (long category : categories) {
      List<ChannelSegmentEntity> filteredEntities = loadEntities(slotId, category, country, targetingPlatform,
          siteRating, osId);
      // Makes sure that there is exactly one entry from each Advertiser.
      for (ChannelSegmentEntity entity : filteredEntities) {
        if(entity.getStatus()) {
          insertChannelSegmentToResultSet(result, entity);
        } 
        logger.debug("AdGroup Dropped due to status - Id:", entity.getAdgroupId());
      }

      if(country != -1) {
        // Load Data for all countries
        List<ChannelSegmentEntity> allCountryEntities = loadEntities(slotId, category, -1, targetingPlatform,
            siteRating, osId);

        // Makes sure that there is exactly one entry from each Advertiser for
        // all countries.
        for (ChannelSegmentEntity entity : allCountryEntities) {
          if(entity.getStatus()) {
            insertChannelSegmentToResultSet(result, entity);
          }
          logger.debug("AdGroup Dropped due to status - Id:", entity.getAdgroupId());
        }
      }
      logger.debug("Number of entries in result:", result.size(), "for", slotId, "_", country, "_", categories);
    }
    if(result.size() == 0)
      logger.debug("No matching records for the request - slot:", slotId, "country:", country, "categories:", categories);
    logger.debug("final selected list of segments : ");
    printSegments(result, logger);
    return result;
  }

  // Loads entities and updates cache if required.
  private List<ChannelSegmentEntity> loadEntities(long slotId, long category, long country, Integer targetingPlatform,
      Integer siteRating, int osId) {
    logger.debug("Loading entities for slot:", slotId, "category:", category, "country:", country, "targetingPlatform:", targetingPlatform, "siteRating:", siteRating, "osId:", osId);
    ArrayList<ChannelSegmentEntity> filteredEntities = new ArrayList<ChannelSegmentEntity>();
    Collection<ChannelSegmentEntity> entitiesAllOs = channelAdGroupRepository.getEntities(slotId, category, country,
        targetingPlatform, siteRating, -1);
    Collection<ChannelSegmentEntity> entities = channelAdGroupRepository.getEntities(slotId, category, country,
        targetingPlatform, siteRating, osId);
    filteredEntities.addAll(entitiesAllOs);
    filteredEntities.addAll(entities);
    return filteredEntities;

  }

  private void insertChannelSegmentToResultSet(Map<String, HashMap<String, ChannelSegment>> result,
      ChannelSegmentEntity channelSegmentEntity) {
    if(Filters.advertiserIdtoNameMapping.containsKey(channelSegmentEntity.getAdvertiserId())) {
      InspectorStats
          .initializeFilterStats(Filters.advertiserIdtoNameMapping.get(channelSegmentEntity.getAdvertiserId()));
      InspectorStats.incrementStatCount(Filters.advertiserIdtoNameMapping.get(channelSegmentEntity.getAdvertiserId()),
          InspectorStrings.totalMatchedSegments);
    }

    ChannelSegment channelSegment = createSegment(channelSegmentEntity);

    if(result.get(channelSegmentEntity.getAdvertiserId()) == null) {
      HashMap<String, ChannelSegment> hashMap = new HashMap<String, ChannelSegment>();
      hashMap.put(channelSegmentEntity.getAdgroupId(), channelSegment);
      result.put(channelSegmentEntity.getAdvertiserId(), hashMap);
    } else {
      HashMap<String, ChannelSegment> hashMap = result.get(channelSegmentEntity.getAdvertiserId());
      hashMap.put(channelSegmentEntity.getAdgroupId(), channelSegment);
      result.put(channelSegmentEntity.getAdvertiserId(), hashMap);
    }

  }

  private ChannelSegment createSegment(ChannelSegmentEntity channelSegmentEntity) {
    ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId());
    ChannelFeedbackEntity channelFeedbackEntity = repositoryHelper.queryChannelFeedbackRepository(channelSegmentEntity
        .getAdvertiserId());
    ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper
        .queryChannelSegmentFeedbackRepository(channelSegmentEntity.getAdgroupId());
    SiteFeedbackEntity siteFeedbackEntity = repositoryHelper.querySiteCitrusLeafFeedbackRepository(
        sasParams.getSiteId(), Long.valueOf(sasParams.getSiteIncId()).toString(), logger);
    ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity = null;
    if(channelEntity == null) {
      logger.debug("No channelEntity for advertiserID", channelSegmentEntity.getAdvertiserId());
      channelEntity = MatchSegments.defaultChannelEntity;
    }

    if(channelFeedbackEntity == null) {
      logger.debug("No channelFeedbackEntity for advertiserID", channelSegmentEntity.getAdvertiserId());
      channelFeedbackEntity = MatchSegments.defaultChannelFeedbackEntity;
    }

    if(channelSegmentFeedbackEntity == null) {
      logger.debug("No channelSegmentFeedackEntity for advertiserID", channelSegmentEntity.getAdvertiserId(),
          "and AdgroupId", channelSegmentEntity.getAdgroupId());
      channelSegmentFeedbackEntity = MatchSegments.defaultChannelSegmentFeedbackEntity;
    }

    if(siteFeedbackEntity != null) {
      channelSegmentCitrusLeafFeedbackEntity = siteFeedbackEntity.getAdGroupFeedbackMap().get(
          channelSegmentEntity.getAdgroupId());
    }

    if(channelSegmentCitrusLeafFeedbackEntity == null) {
      logger.debug("No channelSegmentFeedackEntity for advertiserID", channelSegmentEntity.getAdvertiserId(),
          "and AdgroupId", channelSegmentEntity.getAdgroupId());
      channelSegmentCitrusLeafFeedbackEntity = MatchSegments.defaultChannelSegmentCitrusLeafFeedbackEntity;
    }

    double pECPM = channelSegmentFeedbackEntity.geteCPM();
    return new ChannelSegment(channelSegmentEntity, channelEntity, channelFeedbackEntity, channelSegmentFeedbackEntity,
        channelSegmentCitrusLeafFeedbackEntity, null, pECPM);
  }

  public static void printSegments(Map<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger) {
    if(logger.isDebugEnabled()) {
      logger.debug("Segments are :");
      for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
        Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
        for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
          ChannelSegment channelSegment = adGroupEntry.getValue();
          logger.debug("Advertiser is", channelSegment.getChannelSegmentEntity().getAdvertiserId(), "and AdGp is",
              channelSegment.getChannelSegmentEntity().getAdgroupId(), "ecpm is", channelSegment.getPrioritisedECPM());
        }
      }
    }
  }
}
