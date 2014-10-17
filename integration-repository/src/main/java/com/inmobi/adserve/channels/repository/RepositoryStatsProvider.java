package com.inmobi.adserve.channels.repository;

import java.util.Formatter;
import java.util.List;

import lombok.NoArgsConstructor;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import com.inmobi.phoenix.batteries.data.stats.DBRepositoryStats;


@NoArgsConstructor
public class RepositoryStatsProvider {

  private final List<AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?>> repositories = Lists
      .newArrayList();

  public RepositoryStatsProvider addRepositoryToStats(
      final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository) {
    repositories.add(repository);
    return this;
  }

  public String getStats() throws JSONException {
    final JSONObject jsonObject = new JSONObject();
    for (final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository : repositories) {
      jsonObject.put(repository.getClass().getSimpleName(), new JSONObject(formatStats(repository.getStats())));
    }
    return jsonObject.toString();
  }

  public String getErrorDetails() throws JSONException {
    final JSONObject jsonObject = new JSONObject();
    for (final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository : repositories) {
      jsonObject.put(repository.getClass().getSimpleName(), new JSONObject(repository.getErrorDetails()));
    }
    return jsonObject.toString();
  }

  private String formatStats(final DBRepositoryStats stats) {
    final Formatter formatter = new Formatter();

    formatter.format("{ \"stats\": " + "{ \"age\": %d, " + "\"lastSuccessfulUpdate\"  : %d, "
        + "\"timeForUpdate\"  : %d, " + "\"entities\": %d, " + "\"updatedEntities\" : %d, "
        + "\"skippedEntities\"  : %d, " + "\"isUpdating\"  : %s, " + "\"noOfUpdates\"  : %d, "
        + "\"noOfSuccessfulUpdates\"  : %d, " + "\"noOfUnsuccessfulUpdates\"  : %d} " + "} ",
        stats.getLastUpdateTime(), stats.getLastSuccessfulUpdateTime(), stats.getTimeForUpdate(),
        stats.getEntityCount(), stats.getUpdatedEntityCount(), stats.getSkippedEntityCount(), stats.isUpdating(),
        stats.getUpdates(), stats.getSuccessfulUpdates(), stats.getUnsuccessfulUpdates());

    final String statString = formatter.toString();
    formatter.close();

    return statString;
  }
}
