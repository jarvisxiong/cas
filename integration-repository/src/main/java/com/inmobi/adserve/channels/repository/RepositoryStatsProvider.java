package com.inmobi.adserve.channels.repository;

import com.google.common.collect.Lists;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import com.inmobi.phoenix.batteries.data.stats.DBRepositoryStats;
import com.inmobi.phoenix.batteries.data.stats.RepositoryStats;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Formatter;
import java.util.List;


@NoArgsConstructor
public class RepositoryStatsProvider {

    private final List<AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?>> repositories = Lists
                                                                                                                      .newArrayList();

    public RepositoryStatsProvider addRepositoryToStats(
            AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository) {
        this.repositories.add(repository);
        return this;
    }

    public String getStats() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository : repositories) {
            jsonObject.put(repository.getClass().getSimpleName(), new JSONObject(formatStats(repository.getStats())));
        }
        return jsonObject.toString();
    }

    public String getErrorDetails() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository : repositories) {
            jsonObject.put(repository.getClass().getSimpleName(), new JSONObject(repository.getErrorDetails()));
        }
        return jsonObject.toString();
    }

    private String formatStats(DBRepositoryStats stats) {
        Formatter formatter = new Formatter();

        formatter.format("{ \"stats\": " + "{ \"age\": %d, " + "\"lastSuccessfulUpdate\"  : %d, "
                + "\"timeForUpdate\"  : %d, " + "\"entities\": %d, " + "\"updatedEntities\" : %d, "
                + "\"skippedEntities\"  : %d, " + "\"isUpdating\"  : %s, " + "\"noOfUpdates\"  : %d, "
                + "\"noOfSuccessfulUpdates\"  : %d, " + "\"noOfUnsuccessfulUpdates\"  : %d} " + "} ",
            stats.getLastUpdateTime(), stats.getLastSuccessfulUpdateTime(), stats.getTimeForUpdate(),
            stats.getEntityCount(), stats.getUpdatedEntityCount(), stats.getSkippedEntityCount(), stats.isUpdating(),
            stats.getUpdates(), stats.getSuccessfulUpdates(), stats.getUnsuccessfulUpdates());

        String statString = formatter.toString();
        formatter.close();

        return statString;
    }
}
