package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private static final String DEFAULT = "default";
  private RepositoryHelper repositoryHelper;
  private SASRequestParameters sasParams;
  private SiteFeedbackEntity siteFeedbackEntity;
  private static ChannelAdGroupRepository channelAdGroupRepository;
  private static ChannelEntity defaultChannelEntity;
  private static ChannelFeedbackEntity defaultChannelFeedbackEntity;
  private static ChannelSegmentFeedbackEntity defaultChannelSegmentFeedbackEntity;
  private static ChannelSegmentFeedbackEntity defaultChannelSegmentCitrusLeafFeedbackEntity;

  public static void init(ChannelAdGroupRepository channelAdGroupRepository) {
    Set<String> emptySet = new HashSet<String>();
    MatchSegments.channelAdGroupRepository = channelAdGroupRepository;
    
    ChannelEntity channelEntity = new ChannelEntity();
    channelEntity.setImpressionCeil(Long.MAX_VALUE);
    channelEntity.setImpressionFloor(0);
    channelEntity.setPriority(3);
    channelEntity.setRequestCap(Long.MAX_VALUE);
    MatchSegments.defaultChannelEntity = channelEntity;
        
    MatchSegments.defaultChannelEntity.setSiteInclusion(false);
    MatchSegments.defaultChannelEntity.setSitesIE(emptySet);
    MatchSegments.defaultChannelFeedbackEntity = new ChannelFeedbackEntity(DEFAULT, 0, 0, Double.MAX_VALUE, 0, 0, 0, 0,
        0);
    Double defaultEcpm = ServletHandler.getServerConfig().getDouble("default.ecpm", 0.1);
    MatchSegments.defaultChannelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(DEFAULT, DEFAULT,
        defaultEcpm, 0.5, 0, 0, 0, 0);
    MatchSegments.defaultChannelSegmentCitrusLeafFeedbackEntity = new ChannelSegmentFeedbackEntity(DEFAULT, DEFAULT,
        defaultEcpm, 0.01, 400, 0, 0, 0);
  }

  public MatchSegments(RepositoryHelper repositoryHelper, SASRequestParameters sasParams, DebugLogger logger) {
    this.repositoryHelper = repositoryHelper;
    this.sasParams = sasParams;
    this.logger = logger;
    this.siteFeedbackEntity = repositoryHelper.querySiteCitrusLeafFeedbackRepository(sasParams.getSiteId(), sasParams
        .getSiteSegmentId().toString(), logger);
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
      logger.debug("Request# slot:", slotStr, "country:", countryStr, "categories:", sasParams.getCategories(),
          "targetingPlatform:", targetingPlatform, "siteRating:", siteRating, "osId", osId);
      long slot = Long.parseLong(slotStr);
      long country = -1;
      if(countryStr != null) {
        country = Long.parseLong(countryStr);
      }
      return (matchSegments(logger, slot, getCategories(), country, targetingPlatform, siteRating, osId));
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
  public List<Long> getCategories() {
    // Computing all the parents for categories in the category list from the
    // request
    HashSet<Long> categories = new HashSet<Long>();
    if(null != sasParams.getCategories()) {
      for (Long cat : sasParams.getCategories()) {
        String parentId = cat.toString();
        while (parentId != null) {
          categories.add(Long.parseLong(parentId));
          SiteTaxonomyEntity entity = repositoryHelper.querySiteTaxonomyRepository(parentId);
          if(entity == null) {
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
    HashMap<String /* advertiserId */, HashMap<String /* adGroupId */, ChannelSegment>> result = new HashMap<String /* advertiserId */, HashMap<String /* adGroupId */, ChannelSegment>>();

    //adding -1 for all categories
    categories.add(-1l);
    //adding -1 for all countries
    long[] countries = {-1};
    if (country != -1) {
    	countries = new long[]{-1, country};
    }
    //adding -1 for all osids
    int[] osIds = new int[]{-1, osId};
    
    for(long category : categories) {
    	for (long countryId : countries) {
    		for (int os : osIds) {
    			List<ChannelSegmentEntity> filteredEntities = loadEntities(slotId, category, countryId, targetingPlatform,
    			        siteRating, os);
    			logger.debug("Found", filteredEntities.size(), "segments for slot:", slotId,  "country:", countryId, "category",
    			          category, "targetingPlatform", targetingPlatform, "siteRating", siteRating, "osId", os);
    			logger.debug("Segments are :");
    			
    			for (ChannelSegmentEntity entity : filteredEntities) {
    				logger.debug("AdgroupId", entity.getAdgroupId());
    		        insertChannelSegmentToResultSet(result, entity);
    		    }
    			
    		}
    	}
    }
    
    if(result.size() == 0) {
      logger.debug("No matching records for the request - slot:", slotId, "country:", country, "categories:",
          categories, "targetingPlatform", targetingPlatform, "siteRating", siteRating, "osId", osId);
    }
    logger.debug("final selected list of segments : ");
    printSegments(result, logger);
    return result;
  }

  // Loads entities and updates cache if required.
  private List<ChannelSegmentEntity> loadEntities(long slotId, long category, long country, Integer targetingPlatform,
      Integer siteRating, int osId) {
    logger.debug("Loading entities for slot:", slotId, "category:", category, "country:", country,
        "targetingPlatform:", targetingPlatform, "siteRating:", siteRating, "osId:", osId);
    ArrayList<ChannelSegmentEntity> filteredEntities = new ArrayList<ChannelSegmentEntity>();
    Collection<ChannelSegmentEntity> entities = channelAdGroupRepository.getEntities(slotId, category, country,
        targetingPlatform, siteRating, osId);
    filteredEntities.addAll(entities);
    return filteredEntities;
  }

  private void insertChannelSegmentToResultSet(Map<String, HashMap<String, ChannelSegment>> result,
      ChannelSegmentEntity channelSegmentEntity) {
    if(Filters.advertiserIdtoNameMapping.containsKey(channelSegmentEntity.getAdvertiserId())) {
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
      logger.debug("siteFeedbackEntity is", siteFeedbackEntity);
      if(siteFeedbackEntity.getAdGroupFeedbackMap() != null)
        channelSegmentCitrusLeafFeedbackEntity = siteFeedbackEntity.getAdGroupFeedbackMap().get(
            channelSegmentEntity.getExternalSiteKey());
    } else {
      logger.debug("siteFeedbackEntity is null");
    }

    if(channelSegmentCitrusLeafFeedbackEntity == null) {
      logger.debug("No channelSegmentCitrusLeafFeedbackEntity for advertiserID",
          channelSegmentEntity.getAdvertiserId(), "and ExternalSiteKey", channelSegmentEntity.getExternalSiteKey());
      channelSegmentCitrusLeafFeedbackEntity = MatchSegments.defaultChannelSegmentCitrusLeafFeedbackEntity;
    } else {
      logger.debug("Found channelSegmentCitrusLeafFeedbackEntity for advertiserID",
          channelSegmentEntity.getAdvertiserId(), "and ExternalSiteKey", channelSegmentEntity.getExternalSiteKey(),
          channelSegmentCitrusLeafFeedbackEntity.toString());
    }

    double pECPM = channelSegmentCitrusLeafFeedbackEntity.getECPM();
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
