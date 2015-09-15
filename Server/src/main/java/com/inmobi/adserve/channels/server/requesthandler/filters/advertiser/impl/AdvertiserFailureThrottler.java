package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.circuitbreaker.CircuitBreakerInterface;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelThrottler;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ExceptionBlock;

/**
 * This class can be extend to throttle request sent to the advertiser/partner on failures. Here failure means timeouts
 * and terminates. If the success ratio for a partner falls below threshold(50%), then the circuit is opened and no
 * request flows to the partner for next 5 minutes.
 * 
 * @author rajashekhar.c
 *
 */
public class AdvertiserFailureThrottler extends AbstractAdvertiserLevelThrottler {
    private final static Logger LOG = LoggerFactory.getLogger(AdvertiserFailureThrottler.class);
    private static Map<String, CircuitBreakerInterface> circuitBreakerMap;
    private static Map<String, AdapterConfig> advertiserIdConfigMap;

    @Inject
    public AdvertiserFailureThrottler(final Provider<Marker> traceMarkerProvider,
            final Map<String, AdapterConfig> advertiserIdConfigMap) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_FAILURE_TROTTLER_FILTER, advertiserIdConfigMap);
        AdvertiserFailureThrottler.advertiserIdConfigMap = advertiserIdConfigMap;
        circuitBreakerMap = new ConcurrentHashMap<String, CircuitBreakerInterface>();
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
        final AdapterConfig adapterConfig = advertiserIdConfigMap.get(channelSegment.getChannelEntity().getAccountId());
        final String advertiserName = adapterConfig.getAdapterName();

        if (CasConfigUtil.getServerConfig().getBoolean("circuitbreaker.disable", false)) {
            return false;
        }

        if (CasConfigUtil.getAdapterConfig().getBoolean(advertiserName + ".excludeCircuitBreaker", false)) {
            return false;
        }

        final boolean value = super.failedInFilter(channelSegment, sasParams);
        return value;
    }

    @Override
    protected CircuitBreakerInterface getRequestsThrottlerMovingWindowCounter(final String advertiserName) {
        return circuitBreakerMap.get(advertiserName);
    }


    /**
     * For every advertiser, failure counter is increased on every failure. Here failure means timeouts + terminates
     *
     * @param advertiserid: Name of the advertiser
     * @param startTime: It it the time at which the request was received to us by the UMP
     */
    public static void increamentRequestsThrottlerCounter(final String advertiserid, final long startTime) {
        if (StringUtils.isNotEmpty(advertiserid)) {
            CircuitBreakerInterface circuitBreaker = circuitBreakerMap.get(advertiserid);
            if (circuitBreaker == null) {
                circuitBreaker = addCircuitBreakerEntryToMap(advertiserid);
            }
            if (circuitBreaker != null) {
                circuitBreaker.increamentFailureCounter(startTime);
            }
        }
    }

    /**
     * For every advertiser, request counter is increased on every request sent to the advertiser
     *
     * @param advertiserid: Name of the advertiser
     * @param startTime: It it the time at which the request was received to us by the UMP
     */
    public static void increamentRequestsCounter(final String advertiserid, final long startTime) {
        CircuitBreakerInterface circuitBreaker = circuitBreakerMap.get(advertiserid);
        if (circuitBreaker == null) {
            circuitBreaker = addCircuitBreakerEntryToMap(advertiserid);
        }
        if (circuitBreaker != null) {
            circuitBreaker.increamentRequestCounter(startTime);
        }
    }

    synchronized private static CircuitBreakerInterface addCircuitBreakerEntryToMap(final String advertiserid) {
        CircuitBreakerInterface circuitBreaker = circuitBreakerMap.get(advertiserid);
        if (circuitBreakerMap.get(advertiserid) == null) {
            try {
                final AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserid);
                final String advertiserName = adapterConfig.getAdapterName();

                @SuppressWarnings("unchecked")
                final Class<CircuitBreakerInterface> circuitBreakerClass =
                        (Class<CircuitBreakerInterface>) Class.forName(CasConfigUtil.getServerConfig().getString(
                                "circuitbreaker.class"));
                circuitBreaker =
                        circuitBreakerClass.getConstructor(new Class[] {String.class}).newInstance(advertiserName);
                circuitBreakerMap.put(advertiserid, circuitBreaker);
            } catch (final Exception ex) {
                LOG.error("Error instantiating circuit breaker for AdvertiserId ->" + advertiserid + "->"
                        + ExceptionBlock.getStackTrace(ex));
            }

        }
        return circuitBreaker;
    }


}
