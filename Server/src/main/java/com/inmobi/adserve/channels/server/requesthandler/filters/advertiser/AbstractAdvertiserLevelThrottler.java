package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser;

import java.util.Map;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.server.circuitbreaker.CircuitBreakerInterface;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

/**
 * This class can be extend to throttle request sent to the advertiser/partner on various parameters like
 * failure(timeouts+terminates), low fill rates(partners giving fill rates in third decimal range) or partners giving
 * low ecpm
 * 
 * @author rajashekhar.c
 * 
 */
public abstract class AbstractAdvertiserLevelThrottler extends AbstractAdvertiserLevelFilter {

    private static final long START_TIME_OF_APPLICATION = System.currentTimeMillis();
    private static final long TEN_MINUTES = 60 * 10 * 1000;

    private final Map<String, AdapterConfig> advertiserIdConfigMap;

    public AbstractAdvertiserLevelThrottler(final Provider<Marker> traceMarkerProvider, final String inspectorString,
            final Map<String, AdapterConfig> advertiserIdConfigMap) {
        super(traceMarkerProvider, inspectorString);
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    @Override
    protected boolean failedInFilter(ChannelSegment channelSegment, SASRequestParameters sasParams) {
        final AdapterConfig adapterConfig = advertiserIdConfigMap.get(channelSegment.getChannelEntity().getAccountId());
        String advertiserId = adapterConfig.getAdvertiserId();

        CircuitBreakerInterface circuitBreaker = this.getRequestsThrottlerMovingWindowCounter(advertiserId);

        if (circuitBreaker == null) {
            return false;
        }

        if (isLessThanTenMinutes()) {
            return false;
        }


        boolean canForwardTheRequest = circuitBreaker.canForwardTheRequest();

        return !canForwardTheRequest;
    }

    private static boolean isLessThanTenMinutes() {
        return System.currentTimeMillis() - START_TIME_OF_APPLICATION < TEN_MINUTES;
    }

    protected abstract CircuitBreakerInterface getRequestsThrottlerMovingWindowCounter(String advertiserName);



}
