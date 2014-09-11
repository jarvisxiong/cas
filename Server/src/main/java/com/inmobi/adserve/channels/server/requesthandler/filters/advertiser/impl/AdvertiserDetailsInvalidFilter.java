package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserDetailsInvalidFilter extends AbstractAdvertiserLevelFilter {

    private final Map<String, AdapterConfig> advertiserIdConfigMap;

    @Inject
    public AdvertiserDetailsInvalidFilter(final Provider<Marker> traceMarkerProvider,
            final Map<String, AdapterConfig> advertiserIdConfigMap) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_INVALID_DETAILS_FILTER);
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {

        AdapterConfig adapterConfig = advertiserIdConfigMap.get(channelSegment.getChannelEntity().getAccountId());

        if (adapterConfig == null) {
            return true;
        }

        if (!adapterConfig.isValidHost()) {
            return true;
        }

        if (!adapterConfig.isActive()) {
            return true;
        }

        return false;

    }

}
