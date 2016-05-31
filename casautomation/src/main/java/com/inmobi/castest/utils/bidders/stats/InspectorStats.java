package com.inmobi.castest.utils.bidders.stats;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.reporting.GraphiteReporter;

public class InspectorStats {
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>> ingrapherCounterStats =
            new ConcurrentHashMap<>();
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerCounterStats =
            new ConcurrentHashMap<>();
    private static Map<String, ConcurrentHashMap<String, Histogram>> yammerTimerStats = new ConcurrentHashMap<>();

    private static String STATS = "stats";
    private static String WORK_FLOW = "WorkFlow";

    public static void init(final String graphiteServer, final int graphitePort, final int graphiteInterval) {
        String metricProducer;
        try {
            metricProducer = metricsPrefix(InetAddress.getLocalHost().getHostName().toLowerCase());
        } catch (final UnknownHostException e) {
            metricProducer = "unknown-host";
        }
        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, metricProducer);
    }

    private static String metricsPrefix(String hostname) {
        hostname = StringUtils.removeEnd(hostname, ".inmobi.com");
        return StringUtils.reverseDelimited(hostname, '.');
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
                    ingrapherCounterStats.put(key, new ConcurrentHashMap<>());
                }

            }
        }

        if (ingrapherCounterStats.get(key).get(STATS) == null) {
            synchronized (parameter) {
                if (ingrapherCounterStats.get(key).get(STATS) == null) {
                    ingrapherCounterStats.get(key).put(STATS, new ConcurrentHashMap<>());
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
        final String fullKey = key + '.' + parameter;

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
                    yammerCounterStats.get(key).get(STATS)
                            .put(parameter, Metrics.newCounter(InspectorStats.class, fullKey));
                }
            }
        }

        yammerCounterStats.get(key).get(STATS).get(parameter).inc(value);
    }

    public static void updateYammerTimerStats(final String dst, final String parameter, final long value) {
        final String fullKey = dst + '.' + parameter;
        if (yammerTimerStats.get(dst) == null) {
            synchronized (parameter) {
                if (yammerTimerStats.get(dst) == null) {
                    yammerTimerStats.put(dst, new ConcurrentHashMap<>());
                }
            }
        }

        if (yammerTimerStats.get(dst).get(parameter) == null) {
            synchronized (parameter) {
                if (yammerTimerStats.get(dst).get(parameter) == null) {
                    yammerTimerStats.get(dst).put(parameter, Metrics.newHistogram(InspectorStats.class, fullKey));
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
        ingrapherCounterStats =
                new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>>();

        final Iterator<Entry<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>>> yammerIterator1 =
                yammerCounterStats.entrySet().iterator();
        while (yammerIterator1.hasNext()) {
            final Entry<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerIterator2 =
                    yammerIterator1.next();
            final Iterator<Entry<String, ConcurrentHashMap<String, Counter>>> yammerIterator3 =
                    yammerIterator2.getValue().entrySet().iterator();
            while (yammerIterator3.hasNext()) {
                final Entry<String, ConcurrentHashMap<String, Counter>> yammerIterator4 = yammerIterator3.next();
                final Iterator<Entry<String, Counter>> yammerIterator5 =
                        yammerIterator4.getValue().entrySet().iterator();
                while (yammerIterator5.hasNext()) {
                    yammerIterator5.next().getValue().clear();
                }
            }
        }
    }

    public static String getStats() {
        final JsonObject statsJson = new JsonObject();
        final Gson gson = new GsonBuilder().create();
        statsJson.add("ingrapherCounterStats", gson.toJsonTree(ingrapherCounterStats));
        return statsJson.toString();
    }

}
