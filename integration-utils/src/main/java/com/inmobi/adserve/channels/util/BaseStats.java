package com.inmobi.adserve.channels.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.GraphiteReporter;

import lombok.Setter;

/**
 * @author ritwik.kumar
 *
 */
public abstract class BaseStats {
    private static final Logger LOG = LoggerFactory.getLogger(BaseStats.class);
    protected static final String STATS = "stat";
    protected String containerName = "localhost";

    private final Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerCounterStats =
            new ConcurrentHashMap<>();

    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Gauge<Long>>>> yammerGaugeStats =
            new ConcurrentHashMap<>();

    private final Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Meter>>> yammerMeterStats =
            new ConcurrentHashMap<>();

    protected final MetricsRegistry REGISTRY = new MetricsRegistry();

    /**
     * Init graphite and Stats metrics
     *
     * @param graphiteInterval - set in minutes
     * @param containerName
     * @param registry
     */
    public void baseInit(final Configuration metricsConfiguration, final int graphiteInterval,
            final String containerName, final MetricsRegistry registry) {

        final String graphiteServer = metricsConfiguration.getString("host", "cas-metrics-relay.corp.inmobi.com");
        final int graphitePort = metricsConfiguration.getInt("port", 2020);
        final String metricsPrefix = metricsConfiguration.getString("prefix");

        // This must be changed when unique container name logic has been decided
        final int splitIndex = containerName.indexOf('.');
        if (-1 == splitIndex) {
            this.containerName = containerName;
        } else {
            this.containerName = containerName.substring(0, splitIndex);
        }
        LOG.error("graphiteServer:{}, graphitePort:{}, graphiteInterval:{}", graphiteServer, graphitePort,
                graphiteInterval);
        LOG.error("metricsPrefix:{}, containerName:{}", metricsPrefix, this.containerName);
        GraphiteReporter.enable(registry, graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort,
                metricsPrefix);
    }


    /**
     *
     * @param key
     * @param parameter
     * @param value
     */
    protected void _incrementYammerCount(final String key, final String parameter, final long value) {
        if (yammerCounterStats.get(key) == null) {
            synchronized (parameter) {
                if (yammerCounterStats.get(key) == null) {
                    yammerCounterStats.put(key, new ConcurrentHashMap<>());
                }
            }
        }
        if (yammerCounterStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (yammerCounterStats.get(key).get(STATS) == null) {
                    yammerCounterStats.get(key).put(STATS, new ConcurrentHashMap<>());
                }
            }
        }
        if (yammerCounterStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerCounterStats.get(key).get(STATS).get(parameter) == null) {
                    final MetricName metricName = new MetricName(containerName, key, parameter);
                    yammerCounterStats.get(key).get(STATS).put(parameter, REGISTRY.newCounter(metricName));
                }
            }
        }
        yammerCounterStats.get(key).get(STATS).get(parameter).inc(value);
    }

    /**
     *
     * @param key
     * @param parameter
     * @param value
     */
    protected void _incrementYammerMeter(final String key, final String parameter, final long value) {
        if (yammerMeterStats.get(key) == null) {
            synchronized (parameter) {
                if (yammerMeterStats.get(key) == null) {
                    yammerMeterStats.put(key, new ConcurrentHashMap<>());
                }
            }
        }
        if (yammerMeterStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (yammerMeterStats.get(key).get(STATS) == null) {
                    yammerMeterStats.get(key).put(STATS, new ConcurrentHashMap<>());
                }
            }
        }
        if (yammerMeterStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerMeterStats.get(key).get(STATS).get(parameter) == null) {
                    final MetricName metricName = new MetricName(containerName, "meterRate." + key, parameter);
                    yammerMeterStats.get(key).get(STATS).put(parameter,
                            REGISTRY.newMeter(metricName, parameter, TimeUnit.MINUTES));
                }
            }
        }
        yammerMeterStats.get(key).get(STATS).get(parameter).mark(value);
    }


    /**
     *
     * @param key
     * @param parameter
     * @param value
     */
    protected void _addYammerGauge(final String key, final String parameter, final long value) {
        if (yammerGaugeStats.get(key) == null) {
            synchronized (parameter) {
                if (yammerGaugeStats.get(key) == null) {
                    yammerGaugeStats.put(key, new ConcurrentHashMap<>());
                }
            }
        }
        if (yammerGaugeStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (yammerGaugeStats.get(key).get(STATS) == null) {
                    yammerGaugeStats.get(key).put(STATS, new ConcurrentHashMap<>());
                }
            }
        }
        if (yammerGaugeStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerGaugeStats.get(key).get(STATS).get(parameter) == null) {
                    final MetricName metricName = new MetricName(containerName, key, parameter);
                    yammerGaugeStats.get(key).get(STATS).put(parameter,
                            REGISTRY.newGauge(metricName, new MetricGauge(value)));
                }
            }
        }
        final MetricGauge gauge = (MetricGauge) yammerGaugeStats.get(key).get(STATS).get(parameter);
        gauge.setValue(value);
    }

    private static class MetricGauge extends Gauge<Long> {
        @Setter
        private Long value;

        MetricGauge(final Long value) {
            this.value = value;
        }

        @Override
        public Long value() {
            return value;
        }

    }

}
