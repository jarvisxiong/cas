package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupDailyImpressionCountFilter extends AbstractAdGroupLevelFilter {

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupDailyImpressionCountFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_DAILY_IMP_COUNT_FILTER);

    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        return channelSegment.getChannelSegmentFeedbackEntity().getTodayImpressions() > channelSegment
                .getChannelSegmentEntity().getImpressionCeil();

    }

}
