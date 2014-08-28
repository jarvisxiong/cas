package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ChannelSegmentMatchingCache {

    private static Logger                                                                     logger;
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, ChannelSegmentEntity>> entityHashMap;

    public static void init(Logger logger) {
        ChannelSegmentMatchingCache.logger = logger;
        entityHashMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ChannelSegmentEntity>>();
    }

    public static Collection<ChannelSegmentEntity> getEntities(long slotId, long category, long country,
            Integer targetingPlatform, Integer siteRating, Integer osId, Integer dst) {
        String key = getKey(slotId, category, country, targetingPlatform, siteRating, osId, dst);
        Map<String, ChannelSegmentEntity> entities = entityHashMap.get(key);
        if (null == entities) {
            logger.debug("Lookup in repository for key: " + key + "returned empty array");
            return Collections.emptySet();
        }
        if (entities.size() == 0) {
            logger.info("No entries found in the database for the key " + key);
        }
        return entities.values();
    }

    static void cleanupEntityFromCache(ChannelSegmentEntity entity, List<String> matchingKeys) {
        for (String key : matchingKeys) {
            removeEntity(key, entity);
        }
    }

    static void removeEntity(String key, ChannelSegmentEntity entity) {
        ConcurrentHashMap<String, ChannelSegmentEntity> map = entityHashMap.get(key);
        if (null != map) {
            map.remove(entity.getAdgroupId());
            logger.debug("removed channel segment with key: " + key + " and AdGroupId: " + entity.getAdgroupId());
        }
    }

    static void insertEntityToCache(ChannelSegmentEntity entity, List<String> matchingKeys) {
        for (String key : matchingKeys) {
            insertEntity(key, entity);
        }
    }

    static void insertEntity(String key, ChannelSegmentEntity entity) {
        ConcurrentHashMap<String, ChannelSegmentEntity> map = entityHashMap.get(key);
        if (null == map) {
            map = new ConcurrentHashMap<String, ChannelSegmentEntity>();
            entityHashMap.put(key, map);
        }
        map.put(entity.getAdgroupId(), entity);
        logger.debug("Updated channel segment with key: " + key + " and AdGroupId: " + entity.getAdgroupId());
    }

    static List<String> generateMatchingKeys(ChannelSegmentEntity entity) {
        Long[] allowedSlots = entity.getSlotIds();
        Long[] allowedCategories = (entity.isAllTags()) ? new Long[] { -1L } : entity.getCategoryTaxonomy();
        Long[] allowedCountries = entity.getRcList() == null || entity.getRcList().length == 0 ? new Long[] { -1L }
                : entity.getRcList();
        List<Integer> allowedTargetingPlatform = entity.getTargetingPlatform();
        Integer[] allowedSiteRatings = entity.getSiteRatings();
        List<Integer> allowedOsIds = entity.getOsIds() == null || entity.getOsIds().size() == 0 ? new ArrayList<Integer>(Arrays.asList(new Integer[] { -1 })) : entity.getOsIds();
        
        Integer dst = entity.getDst();

        List<String> matchingKeys = new ArrayList<String>();
        for (Long slot : allowedSlots) {
            for (Long category : allowedCategories) {
                for (Long country : allowedCountries) {
                    for (Integer targetingPlatform : allowedTargetingPlatform) {
                        for (Integer siteRating : allowedSiteRatings) {
                            for (Integer osId : allowedOsIds) {
                                String key = getKey(slot, category, country, targetingPlatform, siteRating, osId, dst);
                                matchingKeys.add(key);
                            }
                        }
                    }
                }
            }
        }
        return matchingKeys;
    }

    private static String getKey(long slotId, long category, long country, Integer targetingPlatform,
            Integer siteRating, Integer osId, Integer dst) {
        return slotId + "_" + category + "_" + country + "_" + targetingPlatform + "_" + siteRating + "_" + osId + "_" + dst;
    }

}
