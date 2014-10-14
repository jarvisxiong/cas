package com.inmobi.adserve.channels.server;

import lombok.Getter;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * devi.chand@inmobi.com
 */

@Singleton
public class CreativeCache {
    @Getter
    private final static ConcurrentHashMap<String, HashSet<String>> CREATIVE_CACHE = new ConcurrentHashMap<String, HashSet<String>>();

    private final Object lock = new Object();

    @GuardedBy("lock")
    public boolean isPresentInCache(String advertiserId, String creativeId) {
        synchronized (lock) {
            Set<String> creatives = CREATIVE_CACHE.get(advertiserId);
            return null != creatives && creatives.contains(creativeId);
        }
    }

    @GuardedBy("lock")
    public void addToCache(String advertiserId, String creativeId) {
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
    public void removeFromCache(String advertiserId, String creativeId) {
        synchronized (lock) {
            HashSet<String> creatives = CREATIVE_CACHE.get(advertiserId);
            if (null != creatives) {
                creatives.remove(creativeId);
            }
        }
    }
}
