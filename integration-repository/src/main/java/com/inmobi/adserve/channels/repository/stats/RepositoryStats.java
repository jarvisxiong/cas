package com.inmobi.adserve.channels.repository.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.util.BaseStats;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;


/**
 *
 * @author ritwik.kumar
 *
 */
public class RepositoryStats extends BaseStats {
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Gauge<Long>>>> yammerGaugeStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Gauge<Long>>>>();

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
        BaseStats.init(graphiteServer, graphitePort, graphiteInterval, hostName);
    }

    /**
     * Use this to insert a gauge to metrics
     *
     * @param key - A new key apart from WORKFLOW
     * @param parameter - Parameter will go under key
     * @param value
     */
    public static void addYammerGauge(final String key, final String parameter, final long value) {
        if (yammerGaugeStats.get(key) == null) {
            synchronized (parameter) {
                if (yammerGaugeStats.get(key) == null) {
                    yammerGaugeStats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, Gauge<Long>>>());
                }
            }
        }
        if (yammerGaugeStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (yammerGaugeStats.get(key).get(STATS) == null) {
                    yammerGaugeStats.get(key).put(STATS, new ConcurrentHashMap<String, Gauge<Long>>());
                }
            }
        }
        if (yammerGaugeStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerGaugeStats.get(key).get(STATS).get(parameter) == null) {
                    // MetricName(group,type,name) to which the group belongs, according to the format specified, type
                    // is null
                    final MetricName metricName = new MetricName(boxName, key, parameter);
                    yammerGaugeStats.get(key).get(STATS).put(parameter, Metrics.newGauge(metricName, new Gauge<Long>() {
                        @Override
                        public Long value() {
                            return value;
                        }
                    }));
                }
            }
        }
    }

}
