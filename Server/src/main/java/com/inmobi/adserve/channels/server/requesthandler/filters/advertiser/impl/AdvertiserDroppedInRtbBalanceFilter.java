package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.config.AdapterConfig;
import com.inmobi.adserve.channels.server.config.ServerConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserDroppedInRtbBalanceFilter extends AbstractAdvertiserLevelFilter {

    private static final Logger              LOG = LoggerFactory.getLogger(AdvertiserDroppedInRtbBalanceFilter.class);

    private final Map<String, AdapterConfig> advertiserIdConfigMap;

    private final ServerConfig        serverConfiguration;

    @Inject
    public AdvertiserDroppedInRtbBalanceFilter(final Provider<Marker> traceMarkerProvider,
            final Map<String, AdapterConfig> advertiserIdConfigMap, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInRtbBalanceFilter);
        this.advertiserIdConfigMap = advertiserIdConfigMap;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        boolean isRtbPartner = advertiserIdConfigMap.get(advertiserId).isRtb();
        boolean result = false;
        if (isRtbPartner) {
            result = channelSegment.getChannelFeedbackEntity().getBalance() < serverConfiguration
                    .getRtbBalanceFilterAmount();
        }
        return result;
    }

}