package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;

public class ChannelSegmentCache {
  private class ChannelSegments {
    public ArrayList<ChannelSegmentEntity> entities;
    public long timeInMillis;

    public ChannelSegments(ArrayList<ChannelSegmentEntity> entities, long timeInMillis) {
      this.entities = entities;
      this.timeInMillis = timeInMillis;
    }
  }

  ConcurrentHashMap<String, ChannelSegments> cache;
  ConfigurationLoader config;

  // Time for the cache timeout in seconds.
  int cacheTimeOut;

  public ChannelSegmentCache() {
    // To be read from config.
    cacheTimeOut = 300;
    cache = new ConcurrentHashMap<String, ChannelSegments>();
  }

  public ArrayList<ChannelSegmentEntity> query(DebugLogger logger, long slotId, long category, long country, Integer targetingPlatform, Integer siteRating,
      long platform, int osId) {
    String key = getKey(slotId, category, country, targetingPlatform, siteRating, platform, osId);
    ChannelSegments segments = cache.get(key);
    if(null == segments || (System.currentTimeMillis() - segments.timeInMillis) > cacheTimeOut * 1000) {
      if(logger.isDebugEnabled())
        logger.debug("Cache timeout or Cache not found for: " + key);
      return null;
    }
    return segments.entities;
  }

  public void addOrUpdate(DebugLogger logger, long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, long platform, int osId,
      ArrayList<ChannelSegmentEntity> entities) {
    String key = getKey(slotId, category, country, targetingPlatform, siteRating, platform, osId);
    cache.put(key, new ChannelSegments(entities, System.currentTimeMillis()));
    if(logger.isDebugEnabled())
      logger.debug("Cache updated for the key: " + key);
  }

  private String getKey(long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, long platform, int osId) {
    if(osId == -1)
      return slotId + "_" + category + "_" + country + "_" + targetingPlatform + "_" + siteRating + "_" + platform;
    else
      return slotId + "_" + category + "_" + country + "_" + targetingPlatform + "_" + siteRating + "_OS" + osId;

  }
}
