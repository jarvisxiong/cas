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
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
//import com.inmobi.adserve.channels.entity.ChannelSegmentQuery;
import com.inmobi.phoenix.batteries.data.AbstractHashDBUpdatableRepository;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.phoenix.exception.UnpreparedException;

public class ChannelFeedbackRepository extends AbstractHashDBUpdatableRepository<ChannelFeedbackEntity, String> implements RepositoryManager {
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

  @Override
  public Collection<ChannelFeedbackEntity> buildObjectsFromResultSet(ResultSet rs) throws RepositoryException {
    logger.debug("building objects from result set");
    InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.isUpdating, 1);
    skipped = 0L;
    updated = 0L;
    Set<ChannelFeedbackEntity> channelFeedbackEntities = new HashSet<ChannelFeedbackEntity>();
    startTime = System.currentTimeMillis();
    updating = true;
    updates++;
    try {
      while (rs.next()) {
        try {
          logger.debug("result set is not null");
          String advertiserId = rs.getString("id");
          double totalInflow = rs.getDouble("total_inflow");
          double totalBurn = rs.getDouble("total_burn");
          double balance = rs.getDouble("balance");
          double averageLatency = rs.getDouble("average_latency");
          double revenue = rs.getDouble("revenue");
          int totalImpressions = rs.getInt("total_impressions");
          int todayImpressions = rs.getInt("today_impressions");

          ChannelFeedbackEntity channelFeedbackEntity = new ChannelFeedbackEntity(advertiserId, totalInflow, totalBurn, balance, totalImpressions,
              todayImpressions, averageLatency, revenue);

          channelFeedbackEntities.add(channelFeedbackEntity);
          if(logger.isDebugEnabled())
            logger.debug("advertiserId for the loaded channelFeedbackEntity is " + advertiserId);
        } catch (SQLException e) {
          logger.error("exception in rs" + e.getMessage());
          InspectorStats.incrementRepoStatCount("ChannelFeedbackRepository", InspectorStrings.entityFailedtoLoad, increment);
          InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.lastUnsuccessfulUpdate, System.currentTimeMillis());
        }
      }
    } catch (SQLException e) {
      logger.error("exception in rs" + e.getMessage());
      InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.lastUnsuccessfulUpdate, System.currentTimeMillis());
    }

    if(updated != 0)
      InspectorStats.incrementRepoStatCount("ChannelFeedbackRepository", InspectorStrings.successfulUpdates, increment);
    else
      InspectorStats.incrementRepoStatCount("ChannelFeedbackRepository", InspectorStrings.unSuccessfulUpdates, increment);

    updating = false;
    endTime = System.currentTimeMillis();
    successfulupdates++;
    updateTime = endTime - startTime;

    InspectorStats.incrementRepoStatCount("ChannelFeedbackRepository", InspectorStrings.updateLatency, updateTime);
    InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.lastSuccessfulUpdate, endTime);
    InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.entityCurrentlyLoaded, channelFeedbackEntities.size());
    InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.isUpdating, 0);

    return channelFeedbackEntities;
  }

  @Override
  public HashIndexKeyBuilder<ChannelFeedbackEntity> getHashIndexKeyBuilder(String className) {

    return null;
  }

  @Override
  public ChannelFeedbackEntity queryUniqueResult(RepositoryQuery arg0) throws RepositoryException, UnpreparedException {
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
            super.getInstanceName(), super.getLastUpdateTime(), super.getLastSuccessfulUpdateTime(), getTimeForUpdate(), super.getEntityCount(),
            super.getRefreshTime(), getUpdatedEntityCount(), getSkippedEntityCount(), super.getRepoSource(), super.getRepoSourceDesc(), isUpdating(),
            getUpdates(), getSuccessfulUpdates(), getUnSuccessfulUpdates());
    return formatter.toString();
  }
}
