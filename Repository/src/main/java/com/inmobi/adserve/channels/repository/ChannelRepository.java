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
//import com.inmobi.adserve.channels.entity.ChannelSegmentQuery;
import com.inmobi.phoenix.batteries.data.AbstractHashDBUpdatableRepository;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.phoenix.exception.UnpreparedException;

public class ChannelRepository extends AbstractHashDBUpdatableRepository<ChannelEntity, String> implements RepositoryManager {
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
  HashMap<String, HashMap<String /* AdgroupId */, ChannelEntity>> entityHashMap = new HashMap();

  @Override
  public Collection<ChannelEntity> buildObjectsFromResultSet(ResultSet rs) throws RepositoryException {
    logger.debug("building objects from result set");
    // InspectorStats.setStats("ChannelRepository", InspectorStrings.isUpdating,
    // 1);
    skipped = 0L;
    updated = 0L;
    Set<ChannelEntity> thirdPartyNetworks = new HashSet<ChannelEntity>();
    startTime = System.currentTimeMillis();
    updating = true;
    updates++;
    try {
      while (rs.next()) {
        try {
          logger.debug("result set is not null");
          String id = rs.getString("id");
          String name = rs.getString("name");
          String accountId = rs.getString("account_id");
          String reportingApiKey = rs.getString("reporting_api_key");
          String reportingApiUrl = rs.getString("reporting_api_url");
          String username = rs.getString("username");
          String password = rs.getString("password");
          boolean isTestMode = rs.getBoolean("is_test_mode");
          boolean isActive = rs.getBoolean("is_active");
          long burstQps = rs.getLong("burst_qps");
          long impressionCeil = rs.getLong("impression_ceil");
          Timestamp modifiedOn = rs.getTimestamp("modified_on");
          int priority = rs.getInt("priority");
          int demandSourceTypeId = rs.getInt("demand_source_type_id");
          ChannelEntity thirdPartyNetwork = new ChannelEntity();
          thirdPartyNetwork.setId(id);
          thirdPartyNetwork.setName(name);
          thirdPartyNetwork.setAccountId(accountId);
          thirdPartyNetwork.setReportingApiKey(reportingApiKey);
          thirdPartyNetwork.setReportingApiUrl(reportingApiUrl);
          thirdPartyNetwork.setUsername(username);
          thirdPartyNetwork.setPassword(password);
          thirdPartyNetwork.setIsTestMode(isTestMode);
          thirdPartyNetwork.setIsActive(isActive);
          thirdPartyNetwork.setBurstQps(burstQps);
          thirdPartyNetwork.setImpressionCeil(impressionCeil);
          thirdPartyNetwork.setModifiedOn(modifiedOn);
          thirdPartyNetwork.setPriority(priority);
          thirdPartyNetwork.setDemandSourceTypeId(demandSourceTypeId);
          thirdPartyNetworks.add(thirdPartyNetwork);
          if(logger.isDebugEnabled())
            logger.debug("id for the loaded entity is " + id);
        } catch (SQLException e) {
          logger.error("exception in rs" + e.getMessage());
          // InspectorStats.incrementRepoStatCount("ChannelRepository",
          // InspectorStrings.entityFailedtoLoad, increment);
          // InspectorStats.setStats("ChannelRepository",
          // InspectorStrings.lastUnsuccessfulUpdate,
          // System.currentTimeMillis());
        }
      }
    } catch (SQLException e) {
      logger.error("exception in rs" + e.getMessage());
      // InspectorStats.setStats("ChannelRepository",
      // InspectorStrings.lastUnsuccessfulUpdate, System.currentTimeMillis());
    }
    /*
     * if(updated != 0)
     * InspectorStats.incrementRepoStatCount("ChannelRepository",
     * InspectorStrings.successfulUpdates, increment); else
     * InspectorStats.incrementRepoStatCount("ChannelRepository",
     * InspectorStrings.unSuccessfulUpdates, increment);
     */
    updating = false;
    endTime = System.currentTimeMillis();
    successfulupdates++;
    updateTime = endTime - startTime;
    /*
     * InspectorStats.incrementRepoStatCount("ChannelRepository",
     * InspectorStrings.updateLatency, updateTime);
     * InspectorStats.setStats("ChannelRepository",
     * InspectorStrings.lastSuccessfulUpdate, endTime);
     * InspectorStats.setStats("ChannelRepository",
     * InspectorStrings.entityCurrentlyLoaded, entitySet.size());
     * InspectorStats.setStats("ChannelRepository", InspectorStrings.isUpdating,
     * 0);
     */
    return thirdPartyNetworks;
  }

  @Override
  public HashIndexKeyBuilder<ChannelEntity> getHashIndexKeyBuilder(String className) {
    /*
     * if(ChannelSegmentQuery.class.getName().equals(className)) { return new
     * ChannelSegmentQuery(); }
     */
    return null;
  }

  @Override
  public ChannelEntity queryUniqueResult(RepositoryQuery arg0) throws RepositoryException, UnpreparedException {
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
