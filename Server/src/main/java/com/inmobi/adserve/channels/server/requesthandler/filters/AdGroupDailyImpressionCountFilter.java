package com.inmobi.adserve.channels.server.requesthandler.filters;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.config.AdapterConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdGroupDailyImpressionCountFilter extends AbstractAdGroupLevelFilter {
    private static final Logger              LOG = LoggerFactory.getLogger(AdGroupTimeOfDayTargetingFilter.class);
    private final Map<String, AdapterConfig> advertiserIdAdapterConfigMap;

    /**
     * @param traceMarkerProvider
     */
    protected AdGroupDailyImpressionCountFilter(final Provider<Marker> traceMarkerProvider,
            final Map<String, AdapterConfig> advertiserIdAdapterConfigMap) {
        super(traceMarkerProvider, InspectorStrings.totalSelectedSegments);
        this.advertiserIdAdapterConfigMap = advertiserIdAdapterConfigMap;

    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        AdapterConfig adapterConfig = advertiserIdAdapterConfigMap
                .get(channelSegment.getChannelEntity().getAccountId());
        adapterConfig.getMaxSegmentSelectionCount();

        return channelSegment.getChannelSegmentFeedbackEntity().getTodayImpressions() > channelSegment
                .getChannelSegmentEntity().getImpressionCeil();

    }

}