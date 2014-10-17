package com.inmobi.adserve.channels.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

/**
 * Created by yasir.imteyaz on 09/09/14.
 */
public class ChannelSegmentAdvertiserCache {
  private static Logger logger;
  private static ConcurrentHashMap<String, ConcurrentHashMap<String, ChannelSegmentEntity>> entityHashMap;

  public static void init(final Logger logger) {
    ChannelSegmentAdvertiserCache.logger = logger;
    entityHashMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ChannelSegmentEntity>>();
  }

  public static Collection<ChannelSegmentEntity> getEntities(final String advertiserId) {
    final Map<String, ChannelSegmentEntity> entities = entityHashMap.get(advertiserId);
    if (null == entities) {
      logger.debug("Lookup in repository for advertiserId " + advertiserId + "returned empty array.");
      return Collections.emptySet();
    }
    if (entities.isEmpty()) {
      logger.info("No entries found in the database for the advertiser Id: " + advertiserId);
    }
    return entities.values();
  }

  public static void cleanupEntityFromCache(final ChannelSegmentEntity entity) {
    final ConcurrentHashMap<String, ChannelSegmentEntity> map = entityHashMap.get(entity.getAdvertiserId());
    if (null != map) {
      map.remove(entity.getAdgroupId());
      logger.debug("Removed channel segment AdFroupId " + entity.getAdgroupId() + " of advertiser "
          + entity.getAdgroupId());
    }
  }

  static void insertEntityToCache(final ChannelSegmentEntity entity) {
    ConcurrentHashMap<String, ChannelSegmentEntity> map = entityHashMap.get(entity.getAdvertiserId());
    if (null == map) {
      map = new ConcurrentHashMap<String, ChannelSegmentEntity>();
      entityHashMap.put(entity.getAdvertiserId(), map);
    }

    map.put(entity.getAdgroupId(), entity);
    logger.debug("Updated channel segment of advertiser: " + entity.getAdvertiserId() + " and AdGroupId: "
        + entity.getAdgroupId());
  }
}
