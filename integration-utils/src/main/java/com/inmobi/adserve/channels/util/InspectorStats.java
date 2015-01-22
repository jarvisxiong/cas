package com.inmobi.adserve.channels.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yammer.metrics.core.MetricName;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.reporting.GraphiteReporter;


public class InspectorStats {

    private static final Logger LOG = LoggerFactory.getLogger(InspectorStats.class);
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>> ingrapherCounterStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>>();
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerCounterStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>>();
    private static Map<String, ConcurrentHashMap<String, Histogram>> yammerTimerStats =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, Histogram>>();

    private static final String STATS = "stats";
    private static final String WORK_FLOW = "WorkFlow";

    private static final String GOOD = "GOOD";
    private static final String BAD = "BAD";

    private static boolean shouldLog = false;

    public static void init(final Configuration serverConfiguration, final String hostName) {
        final String graphiteServer =
                serverConfiguration.getString("graphiteServer.host", "cas-metrics-relay.uj1.inmobi.com");
        final int graphitePort = serverConfiguration.getInt("graphiteServer.port", 2020);
        final int graphiteInterval = serverConfiguration.getInt("graphiteServer.intervalInMinutes", 1);
        final String metricProducer = getMetricProducer(hostName);
        shouldLog = serverConfiguration.getBoolean("graphiteServer.shouldLogAdapterLatencies", false);

        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, metricProducer);
    }

    protected static String getMetricProducer(String hostName) {
        StringBuilder metricProducer = null;
        String runEnvironment = System.getProperty("run.environment", "test");
        if (StringUtils.isBlank(hostName)) {
            hostName = "unknown-host";
            LOG.error("HostName of box is null while pushing data to graphite");
        }
        final String PROD = "prod";

        if (runEnvironment.equalsIgnoreCase(PROD)) {
            Pattern prodHostPattern = Pattern.compile("(cas\\d{4})\\.ads\\.(lhr1|uh1|uj1|hkg1)\\.inmobi\\.com");
            Matcher prodHostMatcher = prodHostPattern.matcher(hostName);
            if (prodHostMatcher.find()) {
                metricProducer = new StringBuilder(PROD).append(".").append(prodHostMatcher.group(2)).append(".cas-1.app.").append(prodHostMatcher.group(1));
            } else {
                runEnvironment = "test";
                LOG.error("HostName of box is not of format cas<4 digits>.ads.<uh1|uj1|lhr1|hkg1>.inmobi.com");
            }
        }
        if (!runEnvironment.equalsIgnoreCase(PROD)) {
            final int dotIndex = hostName.indexOf('.');
            final String boxName = dotIndex > 0 ? hostName.substring(0, dotIndex) : hostName;
            metricProducer = new StringBuilder("test.cas-1.app.").append(boxName);
        }
        return metricProducer.toString();
    }

    public static void incrementStatCount(final String parameter, final long value) {
        incrementStatCount(WORK_FLOW, parameter, value);
    }

    public static void incrementStatCount(final String parameter) {
        incrementStatCount(WORK_FLOW, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter) {
        incrementStatCount(key, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter, final long value) {
        if (ingrapherCounterStats.get(key) == null) {
            synchronized (parameter) {
                if (ingrapherCounterStats.get(key) == null) {
                    ingrapherCounterStats.put(key,
                            new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
                }

            }
        }

        if (ingrapherCounterStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (ingrapherCounterStats.get(key).get(STATS) == null) {
                    ingrapherCounterStats.get(key).put(STATS, new ConcurrentHashMap<String, AtomicLong>());
                }

            }
        }

        if (ingrapherCounterStats.get(key).get(STATS).get(parameter) == null) {
            synchronized (parameter) {
                if (ingrapherCounterStats.get(key).get(STATS).get(parameter) == null) {
                    ingrapherCounterStats.get(key).get(STATS).put(parameter, new AtomicLong(0L));
                }

            }
        }
        ingrapherCounterStats.get(key).get(STATS).get(parameter).getAndAdd(value);
        incrementYammerCount(key, parameter, value);
    }

    public static void incrementYammerCount(final String key, final String parameter, final long value) {
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
                    //MetricName(group,type,name) to which the group belongs, according to the format specified, type is null
                    final MetricName metricName = new MetricName(key, "", parameter);
                    yammerCounterStats.get(key).get(STATS)
                            .put(parameter, Metrics.newCounter(metricName));
                }
            }
        }

        yammerCounterStats.get(key).get(STATS).get(parameter).inc(value);
    }

    public static void updateYammerTimerStats(final String dst, final long value, final boolean isGood) {
        if (!shouldLog) {
            return;
        }
        if (isGood) {
            updateYammerTimerStats(dst, GOOD, value);
        } else {
            updateYammerTimerStats(dst, BAD, value);
        }
    }

    public static void updateYammerTimerStats(final String dst, final String parameter, final long value) {
        if (yammerTimerStats.get(dst) == null) {
            synchronized (parameter) {
                if (yammerTimerStats.get(dst) == null) {
                    yammerTimerStats.put(dst, new ConcurrentHashMap<String, Histogram>());
                }
            }
        }

        if (yammerTimerStats.get(dst).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerTimerStats.get(dst).get(parameter) == null) {
                    //MetricName(group,type,name) to which the group belongs, according to the format specified, type is null
                    final MetricName metricName = new MetricName(dst, "", parameter);
                    yammerTimerStats.get(dst).put(parameter, Metrics.newHistogram(metricName, true));
                }
            }
        }

        yammerTimerStats.get(dst).get(parameter).update(value);

    }

    public static void resetTimers() {
        final Iterator<Entry<String, ConcurrentHashMap<String, Histogram>>> dstIterator =
                yammerTimerStats.entrySet().iterator();
        while (dstIterator.hasNext()) {
            final Entry<String, ConcurrentHashMap<String, Histogram>> dstPair = dstIterator.next();
            final Iterator<Entry<String, Histogram>> timerIterator = dstPair.getValue().entrySet().iterator();
            while (timerIterator.hasNext()) {
                timerIterator.next().getValue().clear();
                timerIterator.remove();
            }
            dstIterator.remove();
        }
    }


    public static JSONObject getStatsObj() {
        return new JSONObject(ingrapherCounterStats);
    }


    public static String getStatsString() {
        return getStatsObj().toString();
    }



}
