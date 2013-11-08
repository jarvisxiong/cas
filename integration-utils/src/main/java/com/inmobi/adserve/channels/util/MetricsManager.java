package com.inmobi.adserve.channels.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.yammer.metrics.reporting.GraphiteReporter;


public class MetricsManager
{

    private static Map<Integer/* country */, ConcurrentHashMap<Integer/* os */, ConcurrentHashMap<String/* advertiserName */, RealTimeStats>>> realTimeCountryOsAdvertiserStats = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashMap<String, RealTimeStats>>>();
    private static Map<Integer/* country */, ConcurrentHashMap<Integer/* os */, RealTimeStats>>                                                realTimeCountryOsStats           = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, RealTimeStats>>();
    private static Map<Integer/* country */, RealTimeStats>                                                                                    realTimeCountryStats             = new ConcurrentHashMap<Integer, RealTimeStats>();

    public static void init(String graphiteServer, int graphitePort, int graphiteInterval)
    {
        String metricProducer;
        try {
            metricProducer = metricsPrefix(InetAddress.getLocalHost().getHostName().toLowerCase());
        }
        catch (UnknownHostException e) {
            metricProducer = "unknown-host";
        }
        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, metricProducer);
    }

    private static String metricsPrefix(String hostname)
    {
        hostname = StringUtils.removeEnd(hostname, ".inmobi.com");
        return StringUtils.reverseDelimited(hostname, '.');
    }

    private static RealTimeStats getRealTimeCountryOsAdvertiserStats(int countryId, String countryName, int osId,
            String osName, String advertiserName)
    {
        if (null == realTimeCountryOsAdvertiserStats.get(countryId)) {
            realTimeCountryOsAdvertiserStats.put(countryId,
                new ConcurrentHashMap<Integer, ConcurrentHashMap<String, RealTimeStats>>());
        }
        if (null == realTimeCountryOsAdvertiserStats.get(countryId).get(osId)) {
            realTimeCountryOsAdvertiserStats.get(countryId).put(osId, new ConcurrentHashMap<String, RealTimeStats>());
        }
        if (null == realTimeCountryOsAdvertiserStats.get(countryId).get(osId).get(advertiserName)) {
            RealTimeStats realTimeStats = new RealTimeStats(countryName, osName, advertiserName);
            realTimeCountryOsAdvertiserStats.get(countryId).get(osId).put(advertiserName, realTimeStats);
        }
        return realTimeCountryOsAdvertiserStats.get(countryId).get(osId).get(advertiserName);
    }

    private static RealTimeStats getRealTimeCountryOsStats(int countryId, String countryName, int osId, String osName)
    {
        if (null == realTimeCountryOsStats.get(countryId)) {
            realTimeCountryOsStats.put(countryId, new ConcurrentHashMap<Integer, RealTimeStats>());
        }
        if (null == realTimeCountryOsStats.get(countryId).get(osId)) {
            RealTimeStats realTimeStats = new RealTimeStats(countryName, osName);
            realTimeCountryOsStats.get(countryId).put(osId, realTimeStats);
        }
        return realTimeCountryOsStats.get(countryId).get(osId);
    }

    private static RealTimeStats getRealTimeCountryStats(int countryId, String countryName)
    {
        if (null == realTimeCountryStats.get(countryId)) {
            RealTimeStats realTimeStats = new RealTimeStats(countryName);
            realTimeCountryStats.put(countryId, realTimeStats);
        }
        return realTimeCountryStats.get(countryId);
    }

    public static void updateStats(int countryId, String countryName, int osId, String osName, String advertiserName,
            boolean fills, boolean requests, boolean serverImpressions, double bids, long latency, double chargedBids)
    {
        RealTimeStats realTimeCountryOsAdvertiserStats = getRealTimeCountryOsAdvertiserStats(countryId, countryName,
            osId, osName, advertiserName);
        RealTimeStats realTimeCountryOsStats = getRealTimeCountryOsStats(countryId, countryName, osId, osName);
        RealTimeStats realTimeCountryStats = getRealTimeCountryStats(countryId, countryName);

        if (fills) {
            realTimeCountryOsAdvertiserStats.getFills().inc();
            realTimeCountryOsStats.getFills().inc();
            realTimeCountryStats.getFills().inc();
        }
        if (requests) {
            realTimeCountryOsAdvertiserStats.getRequests().inc();
            realTimeCountryOsStats.getRequests().inc();
            realTimeCountryStats.getRequests().inc();
        }
        if (serverImpressions) {
            realTimeCountryOsAdvertiserStats.getServerImpressions().inc();
            realTimeCountryOsStats.getServerImpressions().inc();
            realTimeCountryStats.getServerImpressions().inc();
        }
        if (bids > 0.0) {
            realTimeCountryOsAdvertiserStats.getBids().update((long) bids * 1000);
            realTimeCountryOsStats.getBids().update((long) bids * 1000);
            realTimeCountryStats.getBids().update((long) bids * 1000);
        }
        if (latency > 0) {
            realTimeCountryOsAdvertiserStats.getLatency().update(latency);
            realTimeCountryOsStats.getLatency().update(latency);
            realTimeCountryStats.getLatency().update(latency);
        }
        if (chargedBids > 0.0) {
            realTimeCountryOsAdvertiserStats.getChargedBids().update((long) chargedBids * 1000);
            realTimeCountryOsStats.getChargedBids().update((long) chargedBids * 1000);
            realTimeCountryStats.getChargedBids().update((long) chargedBids * 1000);
        }
    }
}