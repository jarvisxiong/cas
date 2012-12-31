package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.commons.configuration.Configuration;

import org.apache.log4j.Logger;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.exception.RepositoryException;

public class MatchSegments {
  private DebugLogger logger;
  private static ChannelAdGroupRepository channelAdGroupRepository;
  private static InspectorStats inspectorStat;

  public static void init(ChannelAdGroupRepository channelAdGroupRepository, InspectorStats inspectorStat) {
    MatchSegments.channelAdGroupRepository = channelAdGroupRepository;
    MatchSegments.inspectorStat = inspectorStat;
  }

  public MatchSegments(DebugLogger logger) {
    this.logger = logger;

  }

  public HashMap<String, HashMap<String, ChannelSegmentEntity>> matchSegments(DebugLogger logger, long slotId, long[] categories, long country /*
                                                                                                                                                * -
                                                                                                                                                * 1
                                                                                                                                                * for
                                                                                                                                                * all
                                                                                                                                                */,
      Integer targetingPlatform, Integer siteRating, int osId) {
    HashMap<String /* advertiserId */, HashMap<String /* adGroupId */, ChannelSegmentEntity>> result = new HashMap<String /* advertiserId */, HashMap<String /* adGroupId */, ChannelSegmentEntity>>();

    ArrayList<ChannelSegmentEntity> filteredAllCategoriesEntities = loadEntities(slotId, -1 /*
                                                                                             * all
                                                                                             * categories
                                                                                             */, country, targetingPlatform, siteRating, osId);

    // Makes sure that there is exactly one entry from each Advertiser.
    for (ChannelSegmentEntity entity : filteredAllCategoriesEntities) {
      if(entity.getStatus())
        insertEntityToResultSet(result, entity);
      else if(logger.isDebugEnabled())
        logger.debug("AdGroup Dropped due to status - Id: " + entity.getAdgroupId());
    }

    if(logger.isDebugEnabled())
      logger.debug("Number of entries from all categories in result: " + result.size() + result);

    if(country != -1) {
      // Load Data for all countries
      ArrayList<ChannelSegmentEntity> allCategoriesAllCountryEntities = loadEntities(slotId, -1 /*
                                                                                                 * all
                                                                                                 * categories
                                                                                                 */, -1 /*
                                                                                                         * All
                                                                                                         * countries
                                                                                                         */, targetingPlatform, siteRating, osId);

      // Makes sure that there is exactly one entry from each Advertiser for all
      // countries.
      for (ChannelSegmentEntity entity : allCategoriesAllCountryEntities) {
        if(entity.getStatus())
          insertEntityToResultSet(result, entity);
        else if(logger.isDebugEnabled())
          logger.debug("AdGroup Dropped due to status - Id: " + entity.getAdgroupId());
      }
      if(logger.isDebugEnabled())
        logger.debug("Number of entries from all countries and categories in result: " + result.size() + result);
    }

    // Does OR for the categories.
    for (long category : categories) {
      ArrayList<ChannelSegmentEntity> filteredEntities = loadEntities(slotId, category, country, targetingPlatform, siteRating, osId);
      // Makes sure that there is exactly one entry from each Advertiser.
      for (ChannelSegmentEntity entity : filteredEntities) {
        if(entity.getStatus())
          insertEntityToResultSet(result, entity);
        else if(logger.isDebugEnabled())
          logger.debug("AdGroup Dropped due to status - Id: " + entity.getAdgroupId());
      }

      if(country != -1) {
        // Load Data for all countries
        ArrayList<ChannelSegmentEntity> allCountryEntities = loadEntities(slotId, category, -1 /*
                                                                                                * All
                                                                                                * countries
                                                                                                */, targetingPlatform, siteRating, osId);

        // Makes sure that there is exactly one entry from each Advertiser for
        // all countries.
        for (ChannelSegmentEntity entity : allCountryEntities) {
          if(entity.getStatus())
            insertEntityToResultSet(result, entity);
          else if(logger.isDebugEnabled())
            logger.debug("AdGroup Dropped due to status - Id: " + entity.getAdgroupId());
        }
      }
      if(logger.isDebugEnabled())
        logger.debug("Number of entries in result: " + result.size() + "for " + slotId + "_" + country + "_" + categories);
    }
    if(result.size() == 0)
      logger.debug("No matching records for the request - slot: " + slotId + " country: " + country + " categories: " + categories);

    logger.debug("final selected list of segments : ");
    printSegments(result, logger);
    return result;
  }

  // Loads entities and updates cache if required.
  private ArrayList<ChannelSegmentEntity> loadEntities(long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, int osId) {
    if(logger.isDebugEnabled())
      logger.debug("Loading entities for slot: " + slotId + " category: " + category + " country: " + country + " targetingPlatform: " + targetingPlatform
          + " siteRating: " + siteRating + " osId: " + osId);
    ArrayList<ChannelSegmentEntity> filteredEntities = new ArrayList();
    Collection<ChannelSegmentEntity> entitiesAllOs = channelAdGroupRepository.getEntities(slotId, category, country, targetingPlatform, siteRating, -1);
    Collection<ChannelSegmentEntity> entities = channelAdGroupRepository.getEntities(slotId, category, country, targetingPlatform, siteRating, osId);
    filteredEntities.addAll(entitiesAllOs);
    filteredEntities.addAll(entities);
    return filteredEntities;

  }

  private void insertEntityToResultSet(HashMap<String, HashMap<String, ChannelSegmentEntity>> result, ChannelSegmentEntity channelSegmentEntity) {
    if(Filters.advertiserIdtoNameMapping.containsKey(channelSegmentEntity.getId())) {
      InspectorStats.initializeFilterStats(Filters.advertiserIdtoNameMapping.get(channelSegmentEntity.getId()));
      InspectorStats.incrementStatCount(Filters.advertiserIdtoNameMapping.get(channelSegmentEntity.getId()), InspectorStrings.totalMatchedSegments);
    }

    if(result.get(channelSegmentEntity.getId()) == null) {
      HashMap<String, ChannelSegmentEntity> hashMap = new HashMap<String, ChannelSegmentEntity>();
      hashMap.put(channelSegmentEntity.getAdgroupId(), channelSegmentEntity);
      result.put(channelSegmentEntity.getId(), hashMap);
    } else {
      HashMap<String, ChannelSegmentEntity> hashMap = result.get(channelSegmentEntity.getId());
      hashMap.put(channelSegmentEntity.getAdgroupId(), channelSegmentEntity);
      result.put(channelSegmentEntity.getId(), hashMap);
    }

  }

  public static void printSegments(HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments, DebugLogger logger) {
    if(logger.isDebugEnabled())
      logger.debug("Segments are :");
    for (String adkey : matchedSegments.keySet()) {
      for (String gpkey : matchedSegments.get(adkey).keySet()) {
        if(logger.isDebugEnabled())
          logger.debug("Advertiser is " + matchedSegments.get(adkey).get(gpkey).getId() + " and AdGp is "
              + matchedSegments.get(adkey).get(gpkey).getAdgroupId());
      }
    }
  }
}
