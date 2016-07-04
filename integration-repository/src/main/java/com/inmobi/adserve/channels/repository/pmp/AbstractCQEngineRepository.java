package com.inmobi.adserve.channels.repository.pmp;


import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.inmobi.data.repository.DBReaderDelegate;
import com.inmobi.data.repository.ScheduledDbReader;

public abstract class AbstractCQEngineRepository {
    private ScheduledDbReader reader;
    protected Logger logger;

    public void init(final Logger logger, final DataSource dataSource, final Configuration config,
            final String instanceName, final DBReaderDelegate repositoryDelegate) {

        this.logger = logger;
        final String query = config.getString("query");

        // TODO: Where do these metrics go?
        reader =
                new ScheduledDbReader(dataSource, query, null, repositoryDelegate, new MetricRegistry(),
                        getRepositorySchedule(config),
                        newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("repository-update-%d").build()), instanceName);
    }

    protected void start() {
        final String className = getClass().getCanonicalName();

        logger.info("Starting asynchronous updates for " + className);
        reader.startAsync();
        logger.info("Waiting for initial load for " + className);
        reader.awaitRunning();
        logger.info("Initial load complete for " + className);
    }

    public void stop() {
        final String className = getClass().getCanonicalName();

        logger.info("Stopping updates for " + className);
        reader.stopAsync();
    }

    private AbstractScheduledService.Scheduler getRepositorySchedule(final Configuration config) {
        final int initialDelay = Preconditions.checkNotNull(config.getInt("initialDelay"));
        final int refreshTime = Preconditions.checkNotNull(config.getInt("refreshTime"));

        return AbstractScheduledService.Scheduler.newFixedRateSchedule(initialDelay, refreshTime, TimeUnit.SECONDS);
    }
}
