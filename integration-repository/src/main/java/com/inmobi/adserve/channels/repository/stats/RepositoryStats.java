package com.inmobi.adserve.channels.repository.stats;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.util.BaseStats;


/**
 *
 * @author ritwik.kumar
 *
 */
public class RepositoryStats extends BaseStats {
    final static RepositoryStats INSTANCE = new RepositoryStats();

    /**
     * 
     */
    private RepositoryStats() {}

    /**
     * Init graphite and Stats metrics. Graphite Interval is set in minutes.
     *
     * @param serverConfiguration
     * @param hostName
     */
    public static void init(final Configuration serverConfiguration, final String hostName) {
        final String graphiteServer =
                serverConfiguration.getString("graphiteServer.host", "cas-metrics-relay.uj1.inmobi.com");
        final int graphitePort = serverConfiguration.getInt("graphiteServer.port", 2020);
        final int graphiteInterval = serverConfiguration.getInt("graphiteServer.repostat.intervalInMinutes", 15);
        INSTANCE.baseInit(graphiteServer, graphitePort, graphiteInterval, hostName, INSTANCE.REGISTRY);
    }

    /**
     * Use this to increment only yammer.
     *
     * @param key - A new key apart from WORKFLOW
     * @param parameter - Parameter will go under key
     * @param value
     */
    public static void incrementYammerCount(final String key, final String parameter, final long value) {
        INSTANCE._incrementYammerCount(key, parameter, value);
    }

    /**
     * Use this to insert a gauge to metrics
     *
     * @param key - A new key apart from WORKFLOW
     * @param parameter - Parameter will go under key
     * @param value
     */
    public static void addYammerGauge(final String key, final String parameter, final long value) {
        INSTANCE._addYammerGauge(key, parameter, value);
    }

}
