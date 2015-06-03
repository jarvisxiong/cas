package com.inmobi.adserve.channels.repository;

import java.util.Formatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aerospike.client.Log;
import com.google.common.collect.Lists;
import com.inmobi.adserve.channels.repository.stats.RepositoryStats;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import com.inmobi.phoenix.batteries.data.stats.DBRepositoryStats;

public class RepositoryStatsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryStatsProvider.class);
    protected static final String PROD = "prod";
    private static final long FIFTEEN_MIN = 15 * 60 * 1000;
    private static final String REPO_PREFIX = "repoStats.";
    private static final String TIMER_NAME = "Repo-Stat-Yammer-Timer";
    private final TimerTask repoStatYammerTask;
    private final Timer repoStatYammerTimer;
    private final List<AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?>> repositories = Lists
            .newArrayList();

    public RepositoryStatsProvider() {
        repoStatYammerTask = new TimerTask() {

            @Override
            public void run() {
                Log.debug("Pushing repo stats to yammer");
                for (final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repo : repositories) {
                    if (!repo.isUpdating()) {
                        final String repoName = REPO_PREFIX + repo.getRepository().getInstanceName();
                        final DBRepositoryStats stats = repo.getStats();
                        RepositoryStats.addYammerGauge(repoName, "lastUpdateTime", stats.getLastUpdateTime());
                        RepositoryStats.addYammerGauge(repoName, "lastSuccessfulUpdateTime",
                                repo.getLastSuccessfulUpdateTime());
                        RepositoryStats.addYammerGauge(repoName, "timeForUpdate", stats.getTimeForUpdate());
                        RepositoryStats.addYammerGauge(repoName, "entityCount", stats.getEntityCount());
                        RepositoryStats.addYammerGauge(repoName, "updatedEntityCount", stats.getUpdatedEntityCount());
                        RepositoryStats.addYammerGauge(repoName, "skippedEntityCount", stats.getSkippedEntityCount());
                        RepositoryStats.addYammerGauge(repoName, "updates", stats.getUpdates());
                        RepositoryStats.addYammerGauge(repoName, "successfulUpdates", stats.getSuccessfulUpdates());
                        RepositoryStats.addYammerGauge(repoName, "unSuccessfulUpdates", stats.getUnsuccessfulUpdates());
                    }
                }

            }
        };
        repoStatYammerTimer = new Timer("Repo-Stat-Yammer-Timer");
        repoStatYammerTimer.scheduleAtFixedRate(repoStatYammerTask, FIFTEEN_MIN, FIFTEEN_MIN);
        LOG.info("Scheduled {} at delay of {} ms", TIMER_NAME, FIFTEEN_MIN);
    }

    /**
     * 
     * @param repository
     * @return
     */
    public RepositoryStatsProvider addRepositoryToStats(
            final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository) {
        repositories.add(repository);
        return this;
    }

    /**
     * 
     * @return
     * @throws JSONException
     */
    public String getStats() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        for (final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository : repositories) {
            jsonObject.put(repository.getClass().getSimpleName(), new JSONObject(formatStats(repository.getStats())));
        }
        return jsonObject.toString();
    }

    /**
     * 
     * @return
     * @throws JSONException
     */
    public String getErrorDetails() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        for (final AbstractStatsMaintainingDBRepository<? extends IdentifiableEntity<?>, ?> repository : repositories) {
            jsonObject.put(repository.getClass().getSimpleName(), new JSONObject(repository.getErrorDetails()));
        }
        return jsonObject.toString();
    }

    /**
     * 
     * @param stats
     * @return
     */
    private String formatStats(final DBRepositoryStats stats) {
        final Formatter formatter = new Formatter();
        formatter.format("{ \"stats\": " + "{ \"lastUpdateTime\": %d, " + "\"lastSuccessfulUpdate\"  : %d, "
                + "\"timeForUpdate\"  : %d, " + "\"entityCount\": %d, " + "\"updatedEntityCount\" : %d, "
                + "\"skippedEntityCount\"  : %d, " + "\"isUpdating\"  : %s, " + "\"noOfUpdates\"  : %d, "
                + "\"noOfSuccessfulUpdates\"  : %d, " + "\"noOfUnsuccessfulUpdates\"  : %d} " + "} ",
                stats.getLastUpdateTime(), stats.getLastSuccessfulUpdateTime(), stats.getTimeForUpdate(),
                stats.getEntityCount(), stats.getUpdatedEntityCount(), stats.getSkippedEntityCount(),
                stats.isUpdating(), stats.getUpdates(), stats.getSuccessfulUpdates(), stats.getUnsuccessfulUpdates());

        final String statString = formatter.toString();
        formatter.close();
        return statString;
    }
}
