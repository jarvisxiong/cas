package com.inmobi.adserve.channels.server.requesthandler.filters;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdvertiserDailyImpressionCeilingExceededFilter extends AbstractAdvertiserLevelFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AdvertiserDailyImpressionCeilingExceededFilter.class);

    @Inject
    public AdvertiserDailyImpressionCeilingExceededFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.droppedInImpressionFilter);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
        return channelSegment.getChannelFeedbackEntity().getTodayImpressions() > channelSegment
                .getChannelEntity()
                    .getImpressionCeil();
    }

}
