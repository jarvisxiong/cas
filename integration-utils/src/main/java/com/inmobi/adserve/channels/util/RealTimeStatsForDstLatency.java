package com.inmobi.adserve.channels.util;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;

import lombok.Getter;


// Yammer objects for country,os,advertiser
public class RealTimeStatsForDstLatency {

    private static final String SEP = ".";
    @Getter
    private Histogram           latency;
    @Getter
    private Histogram           matchSegmentLatency;
    @Getter
    private Histogram           clientTimerLatency;


    public RealTimeStatsForDstLatency(String dstName) {
        String key = dstName;
        initializeStats(key);
    }

    private void initializeStats(String key) {
    	String latencyKey = key + SEP + "latency";        
        this.latency = Metrics.newHistogram(MetricsManager.class, latencyKey);
        
        String matchSegmentLatencyKey = key + SEP + "matchSegmentLatency";        
        this.matchSegmentLatency = Metrics.newHistogram(MetricsManager.class, matchSegmentLatencyKey);
        
        String clientTimerLatencyKey = key + SEP + "clientTimerLatency";        
        this.clientTimerLatency = Metrics.newHistogram(MetricsManager.class, clientTimerLatencyKey);
    }
}
