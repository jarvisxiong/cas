package com.inmobi.adserve.channels.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;

import com.yammer.metrics.core.Counter;


public class InspectorStats {
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>> stats = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>>();
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerStats = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>>();

    public static void incrementStatCount(final String parameter, final long value) {
        incrementStatCount("WorkFlow", parameter, value);
    }

    public static void incrementStatCount(final String parameter) {
        incrementStatCount("WorkFlow", parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter) {
        incrementStatCount(key, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter, final long value) {
        if (stats.get(key) == null) {
            stats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
            stats.get(key).put("stats", new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get("stats").put(parameter, new AtomicLong(value));
        }
        else if (stats.get(key).get("stats") == null) {
            stats.get(key).put("stats", new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get("stats").put(parameter, new AtomicLong(value));
        }
        else if (stats.get(key).get("stats").get(parameter) == null) {
            stats.get(key).get("stats").put(parameter, new AtomicLong(value));
        }
        else {
            stats.get(key).get("stats").get(parameter).getAndAdd(value);
        }
    }

    public static String getStats() {
        return (new JSONObject(stats).toString());
    }

}
