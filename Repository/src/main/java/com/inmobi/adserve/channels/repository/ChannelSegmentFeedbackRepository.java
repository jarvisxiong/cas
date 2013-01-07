package com.inmobi.adserve.channels.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
//import com.inmobi.adserve.channels.entity.ChannelSegmentQuery;
import com.inmobi.phoenix.batteries.data.AbstractHashDBUpdatableRepository;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.phoenix.exception.UnpreparedException;

public class ChannelSegmentFeedbackRepository extends
    AbstractHashDBUpdatableRepository<ChannelSegmentFeedbackEntity, String> implements RepositoryManager {
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
  Map<String, ChannelEntity> entitySet = new HashMap<String, ChannelEntity>();
  HashMap<String, HashMap<String /* AdgroupId */, ChannelEntity>> entityHashMap = new HashMap<String, HashMap<String, ChannelEntity>>();

  @Override
  public Collection<ChannelSegmentFeedbackEntity> buildObjectsFromResultSet(ResultSet rs) throws RepositoryException {
    logger.debug("building objects from result set");
    InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.isUpdating, 1);
    skipped = 0L;
    updated = 0L;
    Set<ChannelSegmentFeedbackEntity> channelSegmentFeedbackEntities = new HashSet<ChannelSegmentFeedbackEntity>();
    startTime = System.currentTimeMillis();
    updating = true;
    updates++;
    try {
      while (rs.next()) {
        try {
          logger.debug("result set is not null");
          String adGroupId = rs.getString("ad_group_id");
          String advertiserId = rs.getString("advertiser_id");
          double eCPM = rs.getDouble("ecpm");
          double fillRatio = rs.getDouble("fill_ratio");

          ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(advertiserId,
              adGroupId, eCPM, fillRatio);

          channelSegmentFeedbackEntities.add(channelSegmentFeedbackEntity);
          if(logger.isDebugEnabled())
            logger.debug("adgroup id for the loaded channelSegmentFeedbackEntity is " + adGroupId);
        } catch (SQLException e) {
          logger.error("exception in rs" + e.getMessage());
          InspectorStats.incrementRepoStatCount("ChannelSegmentFeedbackRepository",
              InspectorStrings.entityFailedtoLoad, increment);
          InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.lastUnsuccessfulUpdate,
              System.currentTimeMillis());
        }
      }
    } catch (SQLException e) {
      logger.error("exception in rs" + e.getMessage());
      InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.lastUnsuccessfulUpdate,
          System.currentTimeMillis());
    }

    if(updated != 0)
      InspectorStats.incrementRepoStatCount("ChannelSegmentFeedbackRepository", InspectorStrings.successfulUpdates,
          increment);
    else
      InspectorStats.incrementRepoStatCount("ChannelSegmentFeedbackRepository", InspectorStrings.unSuccessfulUpdates,
          increment);

    updating = false;
    endTime = System.currentTimeMillis();
    successfulupdates++;
    updateTime = endTime - startTime;

    InspectorStats.incrementRepoStatCount("ChannelSegmentFeedbackRepository", InspectorStrings.updateLatency,
        updateTime);
    InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.lastSuccessfulUpdate, endTime);
    InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.entityCurrentlyLoaded,
        entitySet.size());
    InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.isUpdating, 0);

    return channelSegmentFeedbackEntities;
  }

  @Override
  public HashIndexKeyBuilder<ChannelSegmentFeedbackEntity> getHashIndexKeyBuilder(String className) {
    /*
     * if(ChannelSegmentQuery.class.getName().equals(className)) { return new
     * ChannelSegmentQuery(); }
     */
    return null;
  }

  @Override
  public ChannelSegmentFeedbackEntity queryUniqueResult(RepositoryQuery arg0) throws RepositoryException,
      UnpreparedException {
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

  public String getStats() {
    Formatter formatter = new Formatter();
    formatter
        .format(
            " \"%s\": { \"stats\": { \"age\": %d, \"lastSuccessfulUpdate\"  : %d, \"timeForUpdate\"  : %d, \"entities\": %d, \"refreshTime\"  : %d, \"updatedEntities\" : %d, \"skippedEntities\"  : %d, \"repoSource\"  : %s, \"query/path\"  : %s, \"isUpdating\"  : %s, \"No_of_Updates\"  : %d, \"no_of_successful_updates\"  : %d, \"no_of_unsuccessful_updates\"  : %d,} } ",
            super.getInstanceName(), super.getLastUpdateTime(), super.getLastSuccessfulUpdateTime(),
            getTimeForUpdate(), super.getEntityCount(), super.getRefreshTime(), getUpdatedEntityCount(),
            getSkippedEntityCount(), super.getRepoSource(), super.getRepoSourceDesc(), isUpdating(), getUpdates(),
            getSuccessfulUpdates(), getUnSuccessfulUpdates());
    return formatter.toString();
  }
}
