package com.inmobi.adserve.channels.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class ChannelSegmentMatchingCache {

    private static Logger logger;
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, ChannelSegmentEntity>> entityHashMap;

    public static void init(final Logger logger) {
        ChannelSegmentMatchingCache.logger = logger;
        entityHashMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ChannelSegmentEntity>>();
    }

    public static Collection<ChannelSegmentEntity> getEntities(final long slotId, final long category,
            final long country, final Integer targetingPlatform, final Integer siteRating, final Integer osId,
            final Integer dst) {
        final String key = getKey(slotId, category, country, targetingPlatform, siteRating, osId, dst);
        final Map<String, ChannelSegmentEntity> entities = entityHashMap.get(key);
        if (null == entities) {
            logger.debug("Lookup in repository for key: " + key + "returned empty array");
            return Collections.emptySet();
        }
        if (entities.isEmpty()) {
            logger.error("No entries found in the database for the key " + key);
        }
        return entities.values();
    }

    static void cleanupEntityFromCache(final ChannelSegmentEntity entity, final List<String> matchingKeys) {
        for (final String key : matchingKeys) {
            removeEntity(key, entity);
        }
    }

    static void removeEntity(final String key, final ChannelSegmentEntity entity) {
        final ConcurrentHashMap<String, ChannelSegmentEntity> map = entityHashMap.get(key);
        if (null != map) {
            map.remove(entity.getAdgroupId());
            logger.debug("removed channel segment with key: " + key + " and AdGroupId: " + entity.getAdgroupId());
        }
    }

    static void insertEntityToCache(final ChannelSegmentEntity entity, final List<String> matchingKeys) {
        for (final String key : matchingKeys) {
            insertEntity(key, entity);
        }
    }

    static void insertEntity(final String key, final ChannelSegmentEntity entity) {
        ConcurrentHashMap<String, ChannelSegmentEntity> map = entityHashMap.get(key);
        if (null == map) {
            map = new ConcurrentHashMap<String, ChannelSegmentEntity>();
            entityHashMap.put(key, map);
        }
        map.put(entity.getAdgroupId(), entity);
        logger.debug("Updated channel segment with key: " + key + " and AdGroupId: " + entity.getAdgroupId());
    }

    static List<String> generateMatchingKeys(final ChannelSegmentEntity entity) {
        final Long[] allowedSlots = entity.getSlotIds();
        final Long[] allowedCategories = entity.isAllTags() ? new Long[] {-1L} : entity.getCategoryTaxonomy();
        final Long[] allowedCountries =
                entity.getRcList() == null || entity.getRcList().length == 0 ? new Long[] {-1L} : entity.getRcList();
        final List<Integer> allowedTargetingPlatform = entity.getTargetingPlatform();
        final Integer[] allowedSiteRatings = entity.getSiteRatings();
        final List<Integer> allowedOsIds =
                entity.getOsIds() == null || entity.getOsIds().isEmpty() ? new ArrayList<Integer>(
                        Arrays.asList(new Integer[] {-1})) : entity.getOsIds();

        final Integer dst = entity.getDst();

        final List<String> matchingKeys = new ArrayList<String>();
        for (final Long slot : allowedSlots) {
            for (final Long category : allowedCategories) {
                for (final Long country : allowedCountries) {
                    for (final Integer targetingPlatform : allowedTargetingPlatform) {
                        for (final Integer siteRating : allowedSiteRatings) {
                            for (final Integer osId : allowedOsIds) {
                                final String key =
                                        getKey(slot, category, country, targetingPlatform, siteRating, osId, dst);
                                matchingKeys.add(key);
                            }
                        }
                    }
                }
            }
        }
        return matchingKeys;
    }

    private static String getKey(final long slotId, final long category, final long country,
            final Integer targetingPlatform, final Integer siteRating, final Integer osId, final Integer dst) {
        return slotId + "_" + category + "_" + country + "_" + targetingPlatform + "_" + siteRating + "_" + osId + "_"
                + dst;
    }

}
