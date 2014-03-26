package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.AdapterType;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdGroupAdapterMismatchFilter extends AbstractAdGroupLevelFilter {
    private static final Logger              LOG = LoggerFactory.getLogger(AdGroupAdapterMismatchFilter.class);
    private final Map<String, AdapterConfig> advertiserIdConfigMap;
    private final ServerConfig               serverConfig;

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupAdapterMismatchFilter(final Provider<Marker> traceMarkerProvider,

    final Map<String, AdapterConfig> advertiserIdConfigMap, final ServerConfig serverConfig) {
        super(traceMarkerProvider, InspectorStrings.ADAPTER_MISMATCH);
        this.advertiserIdConfigMap = advertiserIdConfigMap;
        this.serverConfig = serverConfig;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        AdapterConfig adapterConfig = advertiserIdConfigMap.get(channelSegment.getChannelEntity().getAccountId());
        AdapterType adapterType = adapterConfig.getAdapterType();
        boolean isRtbEnabled = serverConfig.isRtbEnabled();

        // if request is from rtb OR segment belongs to rtb , then if adapter is not rtb's or rtb is disabled -> drop
        // that segment
        if ((sasParams.getDst() == 6 || channelSegment.getChannelSegmentEntity().getDst() == 6)
                && (adapterType != AdapterType.RTB || !isRtbEnabled)) {
            return true;
        }

        // TODO: uncomment in UMP phase 2 release .
        // if request is from dcp OR segment is dcp , then if adapter is not dcp -> drop the segment
        if ((sasParams.getDst() == 2 || channelSegment.getChannelSegmentEntity().getDst() == 2)
                && sasParams.isResponseOnlyFromDcp() && (adapterType != AdapterType.DCP)) {
            return true;
        }

        return false;
    }

}
