package com.inmobi.adserve.channels.server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Singleton;

import lombok.Getter;

/**
 * devi.chand@inmobi.com
 */

@Singleton
public class CreativeCache {
  @Getter
  private final static ConcurrentHashMap<String, HashSet<String>> CREATIVE_CACHE =
      new ConcurrentHashMap<String, HashSet<String>>();

  private final Object lock = new Object();

  @GuardedBy("lock")
  public boolean isPresentInCache(final String advertiserId, final String creativeId) {
    synchronized (lock) {
      final Set<String> creatives = CREATIVE_CACHE.get(advertiserId);
      return null != creatives && creatives.contains(creativeId);
    }
  }

  @GuardedBy("lock")
  public void addToCache(final String advertiserId, final String creativeId) {
    synchronized (lock) {
      HashSet<String> creatives = CREATIVE_CACHE.get(advertiserId);
      if (null == creatives) {
        creatives = new HashSet<String>();
        CREATIVE_CACHE.put(advertiserId, creatives);
      }
      creatives.add(creativeId);
    }
  }

  @GuardedBy("lock")
  public void removeFromCache(final String advertiserId, final String creativeId) {
    synchronized (lock) {
      final HashSet<String> creatives = CREATIVE_CACHE.get(advertiserId);
      if (null != creatives) {
        creatives.remove(creativeId);
      }
    }
  }
}
