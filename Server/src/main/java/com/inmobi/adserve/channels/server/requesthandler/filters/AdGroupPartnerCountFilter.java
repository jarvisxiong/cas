package com.inmobi.adserve.channels.server.requesthandler.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdGroupPartnerCountFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupPartnerCountFilter.class);

    /**
     * @param traceMarkerProvider
     */
    protected AdGroupPartnerCountFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.droppedInImpressionFilter);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        return channelSegment.getChannelSegmentFeedbackEntity().getTodayImpressions() > channelSegment
                .getChannelSegmentEntity().getImpressionCeil();

    }

}
