package com.inmobi.adserve.channels.server.requesthandler.filters;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupSiteFloorFilter extends AbstractAdGroupLevelFilter {

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupSiteFloorFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, null);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        return channelSegment.getChannelSegmentFeedbackEntity().getECPM() < sasParams.getSiteFloor();
    }

    @Override
    protected void incrementStats(final ChannelSegment channelSegment) {
        // No-op
    }

}
