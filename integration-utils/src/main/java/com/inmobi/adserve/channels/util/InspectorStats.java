package com.inmobi.adserve.channels.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;


/**
 * 
 * @author ritwik.kumar
 *
 */
public class InspectorStats extends BaseStats {
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
        final int graphiteInterval = serverConfiguration.getInt("graphiteServer.intervalInMinutes", 1);
        shouldLog = serverConfiguration.getBoolean("graphiteServer.shouldLogAdapterLatencies", false);
        BaseStats.init(graphiteServer, graphitePort, graphiteInterval, hostName);
    }

   

    /**
     * Use this to increment yammer and stats page by value
     * 
     * @param parameter - Parameter will go under WORKFLOW
     * @param value
     */
    public static void incrementStatCount(final String parameter, final long value) {
        incrementStatCount(WORK_FLOW, parameter, value);
    }

    /**
     * Use this to increment yammer and stats page by 1
     * 
     * @param parameter - Parameter will go under WORKFLOW
     */
    public static void incrementStatCount(final String parameter) {
        incrementStatCount(WORK_FLOW, parameter, 1L);
    }

    /**
     * Use this to increment yammer and stats page by 1
     * 
     * @param key - A new key apart from WORKFLOW
     * @param parameter - Parameter will go under key
     */
    public static void incrementStatCount(final String key, final String parameter) {
        incrementStatCount(key, parameter, 1L);
    }

    /**
     * Use this to increment yammer and stats page by value
     * 
     * @param key - A new key apart from WORKFLOW
     * @param parameter - Parameter will go under key
     * @param value
     */
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

    /**
     * Use this to increment only yammer.
     * 
     * @param key - A new key apart from WORKFLOW
     * @param parameter - Parameter will go under key
     * @param value
     */
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
                    // MetricName(group,type,name) to which the group belongs, according to the format specified, type
                    // is null
                    final MetricName metricName = new MetricName(boxName, key, parameter);
                    yammerCounterStats.get(key).get(STATS).put(parameter, Metrics.newCounter(metricName));
                }
            }
        }
        yammerCounterStats.get(key).get(STATS).get(parameter).inc(value);
    }

    /**
     * @param dst
     * @param value
     * @param isGood
     */
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

    /**
     * @param dst
     * @param parameter
     * @param value
     */
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
                    // MetricName(group,type,name) to which the group belongs, according to the format specified, type
                    // is null
                    final MetricName metricName = new MetricName(boxName, dst, parameter);
                    yammerTimerStats.get(dst).put(parameter, Metrics.newHistogram(metricName, true));
                }
            }
        }
        yammerTimerStats.get(dst).get(parameter).update(value);
    }

    /**
     * Resets only Yammer Timer Stats
     */
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

    /**
     * @return JsnonObject of ingrapherCounterStats
     */
    public static JSONObject getStatsObj() {
        return new JSONObject(ingrapherCounterStats);
    }

    /**
     * @return String value of <code>getStatsObj()</code>
     */
    public static String getStatsString() {
        return getStatsObj().toString();
    }

}
