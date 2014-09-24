package com.inmobi.adserve.channels.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;


public class InspectorStats {

    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>> stats = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>>();

    private static final String STATS_STRING = "stats";
    private static final String WORKFLOW_STRING = "WorkFlow";

    public static void incrementStatCount(final String parameter, final long value) {
        incrementStatCount(WORKFLOW_STRING, parameter, value);
    }

    public static void incrementStatCount(final String parameter) {
        incrementStatCount(WORKFLOW_STRING, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter) {
        incrementStatCount(key, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter, final long value) {
        if (stats.get(key) == null) {
            stats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
            stats.get(key).put(STATS_STRING, new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get(STATS_STRING).put(parameter, new AtomicLong(value));
        } else if (stats.get(key).get(STATS_STRING) == null) {
            stats.get(key).put(STATS_STRING, new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get(STATS_STRING).put(parameter, new AtomicLong(value));
        } else if (stats.get(key).get(STATS_STRING).get(parameter) == null) {
            stats.get(key).get(STATS_STRING).put(parameter, new AtomicLong(value));
        } else {
            stats.get(key).get(STATS_STRING).get(parameter).getAndAdd(value);
        }
    }

    public static void setStats(final String parameter, final long value) {
        setStats(WORKFLOW_STRING, parameter, value);
    }

    public static void setStats(final String key, final String parameter, final long value) {
        if (stats.get(key) == null) {
            stats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
            stats.get(key).put(STATS_STRING, new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get(STATS_STRING).put(parameter, new AtomicLong(value));
        } else if (stats.get(key).get(STATS_STRING) == null) {
            stats.get(key).put(STATS_STRING, new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get(STATS_STRING).put(parameter, new AtomicLong(value));
        } else {
            stats.get(key).get(STATS_STRING).put(parameter, new AtomicLong(value));
        }
    }

    public static void setWorkflowStats(final String parameter, final long value) {
        setStats(WORKFLOW_STRING, parameter, value);
    }

    public static String getStats() {
        return (new JSONObject(stats).toString());
    }

    public static void initializeSiteFeedbackStats() {
        stats.put("SiteFeedback", new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
    }
}
