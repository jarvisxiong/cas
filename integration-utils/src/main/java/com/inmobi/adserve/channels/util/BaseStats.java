/**
 *
 */
package com.inmobi.adserve.channels.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
    protected static final String PROD = "prod";
    protected static final String STATS = "stat";
    protected String boxName = "CASTestBox";

    private final Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerCounterStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>>();

    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Gauge<Long>>>> yammerGaugeStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Gauge<Long>>>>();

    private final Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Meter>>> yammerMeterStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Meter>>>();

    protected final MetricsRegistry REGISTRY = new MetricsRegistry();

    /**
     * Init graphite and Stats metrics
     *
     * @param graphiteServer
     * @param graphitePort
     * @param graphiteInterval - set in minutes
     * @param hostName
     * @param registry
     */
    public void baseInit(final String graphiteServer, final int graphitePort, final int graphiteInterval,
            final String hostName, final MetricsRegistry registry) {
        final String metricProducer = _getMetricProducer(hostName);
        LOG.error("graphiteServer:{}, graphitePort:{}, graphiteInterval:{}", graphiteServer, graphitePort,
                graphiteInterval);
        LOG.error("metricProducer:{}, boxName:{}", metricProducer, boxName);
        GraphiteReporter.enable(registry, graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort,
                metricProducer);
    }


    /**
     *
     * @param hostName
     * @return
     */
    protected String _getMetricProducer(String hostName) {
        StringBuilder metricProducer = null;
        String runEnvironment = System.getProperty("run.environment", "test");
        if (StringUtils.isBlank(hostName)) {
            hostName = "unknown-host";
            LOG.error("HostName of box is null while pushing data to graphite");
        }
        if (runEnvironment.equalsIgnoreCase(PROD)) {
            final Pattern prodHostPattern = Pattern.compile("(cas\\d{4})\\.ads\\.(lhr1|uh1|uj1|hkg1)\\.inmobi\\.com");
            final Matcher prodHostMatcher = prodHostPattern.matcher(hostName);
            if (prodHostMatcher.find()) {
                metricProducer =
                        new StringBuilder(PROD).append(".").append(prodHostMatcher.group(2)).append(".cas-1.app");
                boxName = prodHostMatcher.group(1);
            } else {
                runEnvironment = "test";
                LOG.error("HostName of box is not of format cas<4 digits>.ads.<uh1|uj1|lhr1|hkg1>.inmobi.com");
            }
        }
        if (!runEnvironment.equalsIgnoreCase(PROD)) {
            final int dotIndex = hostName.indexOf('.');
            boxName = dotIndex > 0 ? hostName.substring(0, dotIndex) : hostName;
            metricProducer = new StringBuilder("test.cas-1.app");
        }
        return metricProducer.toString();
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
                    yammerCounterStats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>());
                }
            }
        }
        if (yammerCounterStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (yammerCounterStats.get(key).get(STATS) == null) {
                    yammerCounterStats.get(key).put(STATS, new ConcurrentHashMap<String, Counter>());
                }
            }
        }
        if (yammerCounterStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerCounterStats.get(key).get(STATS).get(parameter) == null) {
                    final MetricName metricName = new MetricName(boxName, key, parameter);
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
                    yammerMeterStats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, Meter>>());
                }
            }
        }
        if (yammerMeterStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (yammerMeterStats.get(key).get(STATS) == null) {
                    yammerMeterStats.get(key).put(STATS, new ConcurrentHashMap<String, Meter>());
                }
            }
        }
        if (yammerMeterStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerMeterStats.get(key).get(STATS).get(parameter) == null) {
                    final MetricName metricName = new MetricName(boxName, "meterRate." + key, parameter);
                    yammerMeterStats.get(key).get(STATS)
                            .put(parameter, REGISTRY.newMeter(metricName, parameter, TimeUnit.MINUTES));
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
                    final MetricName metricName = new MetricName(boxName, key, parameter);
                    yammerGaugeStats.get(key).get(STATS)
                            .put(parameter, REGISTRY.newGauge(metricName, new MetricGauge(value)));
                }
            }
        }
        final MetricGauge gauge = (MetricGauge) yammerGaugeStats.get(key).get(STATS).get(parameter);
        gauge.setValue(value);
    }

    private class MetricGauge extends Gauge<Long> {
        @Setter
        private Long value;

        public MetricGauge(final Long value) {
            this.value = value;
        }

        @Override
        public Long value() {
            return value;
        }

    }

}
