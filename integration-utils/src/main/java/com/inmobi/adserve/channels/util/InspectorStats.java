package com.inmobi.adserve.channels.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;


public class InspectorStats
{
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>> stats = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>>();

    public static void incrementStatCount(String parameter, long value)
    {
        incrementStatCount("WorkFlow", parameter, value);
    }

    public static void incrementStatCount(String parameter)
    {
        incrementStatCount("WorkFlow", parameter, 1L);
    }

    public static void incrementStatCount(String key, String parameter)
    {
        incrementStatCount(key, parameter, 1L);
    }

    public static void incrementStatCount(String key, String parameter, long value)
    {
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

    public static void setStats(String parameter, long value)
    {
        setStats("WorkFlow", parameter, value);
    }

    public static void setStats(String key, String parameter, long value)
    {
        if (stats.get(key) == null) {
            stats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
            stats.get(key).put("stats", new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get("stats").put(parameter, new AtomicLong(value));
        }
        else if (stats.get(key).get("stats") == null) {
            stats.get(key).put("stats", new ConcurrentHashMap<String, AtomicLong>());
            stats.get(key).get("stats").put(parameter, new AtomicLong(value));
        }
        else {
            stats.get(key).get("stats").put(parameter, new AtomicLong(value));
        }
    }

    public static void setWorkflowStats(String parameter, long value)
    {
        setStats("WorkFlow", parameter, value);
    }

    public static String getStats(int maxConnections, int droppedConnections)
    {
        Map finalMap = new ConcurrentHashMap();
        stats.get("WorkFlow").get("stats").put("MaxConnections", new AtomicLong(maxConnections));
        stats.get("WorkFlow").get("stats").put("DroppedConnections", new AtomicLong(droppedConnections));
        finalMap.putAll(stats);
        return (new JSONObject(finalMap).toString());
    }

    public static void initializeSiteFeedbackStats()
    {
        stats.put("SiteFeedback", new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());
    }
}
