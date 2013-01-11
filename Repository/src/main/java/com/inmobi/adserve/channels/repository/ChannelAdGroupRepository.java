package com.inmobi.adserve.channels.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentQuery;
import com.inmobi.phoenix.batteries.data.AbstractHashDBUpdatableRepository;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.phoenix.exception.UnpreparedException;

public class ChannelAdGroupRepository extends AbstractHashDBUpdatableRepository<ChannelSegmentEntity, String> implements RepositoryManager {
  private long startTime = 0L;
  private long endTime = 0L;
  private boolean updating = false;
  private long skipped = 0L;
  private long updated = 0L;
  private long updateTime;
  private long successfulupdates = 0L;
  private long updates = 0L;
  private static Long increment = 1L;
  private Timestamp recentObjectModifyTime = new Timestamp(0);
  Map<String, ChannelSegmentEntity> entitySet = new HashMap<String, ChannelSegmentEntity>();
  ConcurrentHashMap<String, HashMap<String /* AdgroupId */, ChannelSegmentEntity>> entityHashMap = new ConcurrentHashMap<String, HashMap<String, ChannelSegmentEntity>>();

  @Override
  public Collection<ChannelSegmentEntity> buildObjectsFromResultSet(ResultSet rs) throws RepositoryException {
    logger.debug("building objects from result set");
    InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.isUpdating, 1);
    skipped = 0L;
    updated = 0L;
    Set<ChannelSegmentEntity> thirdPartyNetworks = new HashSet<ChannelSegmentEntity>();
    startTime = System.currentTimeMillis();
    updating = true;
    updates++;
    try {
      while (rs.next()) {
        try {
          logger.debug("result set is not null");
          String advertiserId = rs.getString("advertiser_id");
          String adgroupId = rs.getString("adgroup_id");
          String adId = rs.getString("ad_id");
          String channelId = rs.getString("channel_id");
          String externalSiteKey = rs.getString("external_site_key");
          String campaignId = rs.getString("campaign_id");
          long incId = rs.getLong("inc_id");
          boolean status = rs.getBoolean("status");
          String pricingModel = rs.getString("pricing_model");
          boolean isTestMode = rs.getBoolean("is_test_mode");
          Timestamp modified_on = rs.getTimestamp("modified_on");
          if((rs.getArray("tags") == null) || (rs.getArray("rc_list") == null) || (rs.getArray("slot_ids") == null) || (rs.getArray("site_ratings") == null))
            continue;
          Integer[] siteRatings = (Integer[]) rs.getArray("site_ratings").getArray();
          Long[] rcList = (Long[]) rs.getArray("rc_list").getArray();
          Long[] slotIds = (Long[]) rs.getArray("slot_ids").getArray();
          Long[] tags = (Long[]) rs.getArray("tags").getArray();
          long platformTargeting = rs.getLong("platform_targeting_int");
          boolean allTags = rs.getBoolean("all_tags");
          int targetingPlatform = rs.getInt("targeting_platform");
          String osVersionTargeting = rs.getString("os_version_targeting");
          ArrayList<Integer> osIds = parseOsIds(osVersionTargeting);

          ChannelSegmentEntity thirdPartyNetwork = new ChannelSegmentEntity(advertiserId, adgroupId, adId, channelId, platformTargeting, rcList, tags, status,
              isTestMode, externalSiteKey, modified_on, campaignId, slotIds, incId, allTags, pricingModel, siteRatings, targetingPlatform, osIds);
          ChannelSegmentEntity oldEntity = entitySet.get(adgroupId);
          entitySet.put(adgroupId, thirdPartyNetwork);
          if(null != oldEntity) {
            cleanupEntity(oldEntity);
          }
          if(status) {
            insertEntityToHashMap(thirdPartyNetwork);
            updated++;
          }
          if(logger.isDebugEnabled())
            logger.debug("adgroup id for the loaded entity is " + adgroupId);
        } catch (SQLException e) {
          logger.error("exception in rs" + e.getMessage());
          InspectorStats.incrementRepoStatCount("ChannelAdGroupRepository", InspectorStrings.entityFailedtoLoad, increment);
          InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.lastUnsuccessfulUpdate, System.currentTimeMillis());
        }
      }
    } catch (SQLException e) {
      logger.error("exception in rs" + e.getMessage());
      InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.lastUnsuccessfulUpdate, System.currentTimeMillis());
    }

    if(updated != 0)
      InspectorStats.incrementRepoStatCount("ChannelAdGroupRepository", InspectorStrings.successfulUpdates, increment);
    else
      InspectorStats.incrementRepoStatCount("ChannelAdGroupRepository", InspectorStrings.unSuccessfulUpdates, increment);
    updating = false;
    endTime = System.currentTimeMillis();
    successfulupdates++;
    updateTime = endTime - startTime;
    InspectorStats.incrementRepoStatCount("ChannelAdGroupRepository", InspectorStrings.updateLatency, updateTime);
    InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.lastSuccessfulUpdate, endTime);
    InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.entityCurrentlyLoaded, entitySet.size());
    InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.isUpdating, 0);
    return thirdPartyNetworks;
  }

  // Made protected for testing visibility.
  public ArrayList<Integer> parseOsIds(String osVersionTargeting) {
    ArrayList<Integer> osIds = null;
    try {
      if(osVersionTargeting != null) {
        JSONArray osIdsJson = new JSONObject(osVersionTargeting).getJSONArray("os");
        osIds = new ArrayList<Integer>(osIdsJson.length());
        for (int index = 0; index < osIdsJson.length(); ++index) {
          osIds.add(osIdsJson.getJSONObject(index).optInt("id"));
        }
      }
    } catch (JSONException e) {
      // Do Nothing
    }
    return osIds;
  }

  @Override
  public HashIndexKeyBuilder<ChannelSegmentEntity> getHashIndexKeyBuilder(String className) {
    if(ChannelSegmentQuery.class.getName().equals(className)) {
      return new ChannelSegmentQuery();
    }
    return null;
  }

  @Override
  public ChannelSegmentEntity queryUniqueResult(RepositoryQuery arg0) throws RepositoryException, UnpreparedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Repository getRepository() {
    return this;
  }

  @Override
  public long getSkippedEntityCount() {
    return skipped;
  }

  @Override
  public long getUpdatedEntityCount() {
    return updated;
  }

  @Override
  public long getTimeForUpdate() {
    return updateTime;
  }

  @Override
  public boolean isUpdating() {
    return updating;
  }

  @Override
  public long getSuccessfulUpdates() {
    return successfulupdates;
  }

  @Override
  public long getUnSuccessfulUpdates() {
    return updates - successfulupdates;
  }

  @Override
  public long getUpdates() {
    return updates;
  }

  @Override
  public Timestamp newUpdateFromResultSetToOptimizeUpdate(ResultSet resultSet) throws RepositoryException {
    addEntities(this.buildObjectsFromResultSet(resultSet));
    return recentObjectModifyTime;
  }

  public ChannelSegmentEntity[] getAllEntities() {
    return entitySet.values().toArray(new ChannelSegmentEntity[0]);
  }


  public Collection<ChannelSegmentEntity> getEntities(long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, Integer osId) {
    String key = getKey(slotId, category, country, targetingPlatform, siteRating, osId);
    HashMap<String, ChannelSegmentEntity> entities = entityHashMap.get(key);
    if(null == entities) {
      if(logger.isDebugEnabled())
        logger.debug("Lookup in repository for key: " + key + " returned empty array");
      return Collections.emptySet();
    }
    if(entities.size() == 0)
      logger.error("No entries found in the database for the key " + key);
    return entities.values();
  }
  
  private void cleanupEntity(ChannelSegmentEntity entity) {
 // Family safe, Maturei
    for (Integer siteRating : entity.getSiteRatings()) {
      // Wap, APP
      for (Integer targetingPlatform : entity.getTargetingPlatform()) {
        for (Long slotId : entity.getSlotIds()) {
          if(entity.getAllTags()) {
            if(entity.getRcList() == null || entity.getRcList().length == 0) {
              if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                removeEntity(slotId, -1 /* All categories */, -1, targetingPlatform, siteRating, entity, -1);
              } else {
                for (Integer id : entity.getOsIds()) {
                  removeEntity(slotId, -1 /* All categories */, -1, targetingPlatform, siteRating, entity, id);
                }
              }
            } else {
              for (Long country : entity.getRcList())
                if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                  removeEntity(slotId, -1 /* All categories */, country, targetingPlatform, siteRating, entity, -1);
                } else {
                  for (Integer id : entity.getOsIds()) {
                    removeEntity(slotId, -1 /* All categories */, country, targetingPlatform, siteRating, entity, id);
                  }
                }
            }
          } else {
            for (Long category : entity.getTags()) {
              if(entity.getRcList() == null || entity.getRcList().length == 0) {
                if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                  removeEntity(slotId, category, -1, targetingPlatform, siteRating, entity, -1);
                } else {
                  for (Integer id : entity.getOsIds()) {
                    removeEntity(slotId, category, -1, targetingPlatform, siteRating, entity, id);
                  }
                }
              } else {
                for (Long country : entity.getRcList())
                  if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                    removeEntity(slotId, category, country, targetingPlatform, siteRating, entity, -1);
                  } else {
                    for (Integer id : entity.getOsIds()) {
                      removeEntity(slotId, category, country, targetingPlatform, siteRating, entity, id);
                    }
                  }
              }
            }
          }
        }
      }
    }
  }

  private void removeEntity(long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, ChannelSegmentEntity entity, Integer osId) {
    String key = getKey(slotId, category, country, targetingPlatform, siteRating, osId);
    HashMap<String, ChannelSegmentEntity> map = entityHashMap.get(key);
    if(null != map) {
      map.remove(entity.getAdgroupId());
      if(logger.isDebugEnabled())
        logger.debug("removed channel segment with key: " + key + " and AdGroupId: " + entity.getAdgroupId());
    }
  }

  private void insertEntityToHashMap(ChannelSegmentEntity entity) {
 // Family safe, Maturei
    for (Integer siteRating : entity.getSiteRatings()) {
      // Wap, APP
      for (Integer targetingPlatform : entity.getTargetingPlatform()) {
        for (Long slotId : entity.getSlotIds()) {
          if(entity.getAllTags()) {
            if(entity.getRcList() == null || entity.getRcList().length == 0) {
              if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                insertEntity(slotId, -1 /* All categories */, -1, targetingPlatform, siteRating, entity, -1);
              } else
                for (Integer id : entity.getOsIds()) {
                  insertEntity(slotId, -1 /* All categories */, -1, targetingPlatform, siteRating, entity, id);
                }
            } else {
              for (Long country : entity.getRcList())
                if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                  insertEntity(slotId, -1 /* All categories */, country, targetingPlatform, siteRating, entity, -1);
                } else
                  for (Integer id : entity.getOsIds()) {
                    insertEntity(slotId, -1 /* All categories */, country, targetingPlatform, siteRating, entity, id);
                  }
            }
          } else {
            for (Long category : entity.getTags()) {
              if(entity.getRcList() == null || entity.getRcList().length == 0) {
                if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                  insertEntity(slotId, category, -1, targetingPlatform, siteRating, entity, -1);
                } else
                  for (Integer id : entity.getOsIds()) {
                    insertEntity(slotId, category, -1, targetingPlatform, siteRating, entity, id);
                  }
              } else {
                for (Long country : entity.getRcList())
                  if(entity.getOsIds() == null || entity.getOsIds().size() == 0) {
                    insertEntity(slotId, category, country, targetingPlatform, siteRating, entity, -1);
                  } else
                    for (Integer id : entity.getOsIds()) {
                      insertEntity(slotId, category, country, targetingPlatform, siteRating, entity, id);
                    }
              }
            }
          }
        }
      }
    }
  }

  private void insertEntity(long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, ChannelSegmentEntity entity, Integer osId) {
    String key = getKey(slotId, category, country, targetingPlatform, siteRating, osId);
    HashMap<String, ChannelSegmentEntity> map = entityHashMap.get(key);
    if(null == map) {
      map = new HashMap<String, ChannelSegmentEntity>();
      entityHashMap.put(key, map);
    }
    map.put(entity.getAdgroupId(), entity);
    if(logger.isDebugEnabled())
      logger.debug("Updated channel segment with key: " + key + " and AdGroupId: " + entity.getAdgroupId());
    
  }

  
  private String getKey(long slotId, long category, long country, Integer targetingPlatform, Integer siteRating, Integer osId) {
    return slotId + "_" + category + "_" + country + "_" + targetingPlatform + "_" + siteRating + "_" + osId;
  }
  

  public String getStats() {
    Formatter formatter = new Formatter();
    formatter
        .format(
            " \"%s\": { \"stats\": { \"age\": %d, \"lastSuccessfulUpdate\"  : %d, \"timeForUpdate\"  : %d, \"entities\": %d, \"refreshTime\"  : %d, \"updatedEntities\" : %d, \"skippedEntities\"  : %d, \"repoSource\"  : %s, \"query/path\"  : %s, \"isUpdating\"  : %s, \"No_of_Updates\"  : %d, \"no_of_successful_updates\"  : %d, \"no_of_unsuccessful_updates\"  : %d,} } ",
            super.getInstanceName(), super.getLastUpdateTime(), super.getLastSuccessfulUpdateTime(), getTimeForUpdate(), super.getEntityCount(),
            super.getRefreshTime(), getUpdatedEntityCount(), getSkippedEntityCount(), super.getRepoSource(), super.getRepoSourceDesc(), isUpdating(),
            getUpdates(), getSuccessfulUpdates(), getUnSuccessfulUpdates());
    String stats = formatter.toString();
    formatter.close();
    return stats;
    
  }
}
