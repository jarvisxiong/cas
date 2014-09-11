package com.inmobi.adserve.channels.util;

import lombok.Getter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;


// Yammer objects for country,os,advertiser
public class RealTimeStatsForPartnerRequests {

    private static final String SEP = ".";
    @Getter
    private Counter             partnerRequests;


    public RealTimeStatsForPartnerRequests(String countryName, String partnerName) {
        String key = countryName + SEP + partnerName;
        initializeStats(key);
    }

    private void initializeStats(String key) {
        String parnterRequestsKey = key + SEP + "partnerRequests";
        
        this.partnerRequests = Metrics.newCounter(MetricsManager.class, parnterRequestsKey);
    }
}
