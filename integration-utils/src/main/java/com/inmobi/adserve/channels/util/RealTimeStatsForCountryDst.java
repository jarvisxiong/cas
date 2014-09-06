package com.inmobi.adserve.channels.util;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import lombok.Getter;


// Yammer objects for country,os,advertiser
public class RealTimeStatsForCountryDst {

    private static final String SEP = ".";
    @Getter
    private Counter             incomingRequests;


    public RealTimeStatsForCountryDst(String countryName, String dstName) {
        String key = countryName + SEP + dstName;
        initializeStats(key);
    }

    private void initializeStats(String key) {
        String incomingRequests = key + SEP + "incomingRequests";
        
        this.incomingRequests = Metrics.newCounter(MetricsManager.class, incomingRequests);
    }
}
