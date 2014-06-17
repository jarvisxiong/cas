package com.inmobi.adserve.channels.util;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import lombok.Getter;


// Yammer objects for country,os,advertiser
public class RealTimeStats {

    private static final String SEP = ".";
    @Getter
    private Counter             fills;
    @Getter
    private Counter             requests;
    @Getter
    private Counter             serverImpressions;
    @Getter
    private Histogram           bids;
    @Getter
    private Histogram           latency;
    @Getter
    private Histogram           chargedBids;      // These are secondBidprices

    public RealTimeStats(String countryName, String osName, String advertiserName) {
        String key = countryName + SEP + osName + SEP + advertiserName;
        initializeStats(key);
    }

    public RealTimeStats(String countryName, String osName) {
        String key = countryName + SEP + osName;
        initializeStats(key);
    }

    public RealTimeStats(String countryName) {
        String key = countryName;
        initializeStats(key);
    }

    private void initializeStats(String key) {
        String fillsKey = key + SEP + "fills";
        String requestsKey = key + SEP + "requests";
        String serverImpressionsKey = key + SEP + "serverImpressions";
        String bidsKey = key + SEP + "bids";
        String latencyKey = key + SEP + "latency";
        String chargedBidsKey = key + SEP + "chargedBids";
        this.fills = Metrics.newCounter(MetricsManager.class, fillsKey);
        this.requests = Metrics.newCounter(MetricsManager.class, requestsKey);
        this.serverImpressions = Metrics.newCounter(MetricsManager.class, serverImpressionsKey);
        this.bids = Metrics.newHistogram(MetricsManager.class, bidsKey);
        this.latency = Metrics.newHistogram(MetricsManager.class, latencyKey);
        this.chargedBids = Metrics.newHistogram(MetricsManager.class, chargedBidsKey);
    }
}
