package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.constants.ChannelSegmentFilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;


/**
 * @author abhishek.parwal
 * 
 */
public abstract class AbstractAdGroupLevelFilter implements AdGroupLevelFilter {

    private static final Logger       LOG = LoggerFactory.getLogger(AbstractAdGroupLevelFilter.class);

    protected final Provider<Marker>  traceMarkerProvider;
    private final String              inspectorString;

    private ChannelSegmentFilterOrder order;

    protected AbstractAdGroupLevelFilter(final Provider<Marker> traceMarkerProvider, final String inspectorString) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.inspectorString = inspectorString;
    }

    @Override
    public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
            final CasContext casContext) {

        Marker traceMarker = traceMarkerProvider.get();

        for (Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
            ChannelSegment channelSegment = iterator.next();

            boolean result = failedInFilter(channelSegment, sasParams, casContext);

            if (result) {
                iterator.remove();
                LOG.debug(traceMarker, "Failed in filter {}  , adgroup {}", this.getClass().getName(), channelSegment
                        .getChannelSegmentFeedbackEntity().getId());
                incrementStats(channelSegment);
            }
            else {
                LOG.debug(traceMarker, "Passed in filter {} ,  advertiser {}", this.getClass().getName(),
                        channelSegment.getChannelSegmentFeedbackEntity().getId());
            }
        }
    }

    /**
     * @param channelSegment
     */
    protected void incrementStats(final ChannelSegment channelSegment) {
        channelSegment.incrementInspectorStats(inspectorString);
    }

    /**
     * @param channelSegment
     * @param sasParams
     * @return
     */
    protected abstract boolean failedInFilter(final ChannelSegment channelSegment,
            final SASRequestParameters sasParams, final CasContext casContext);

    @Override
    final public void setOrder(final ChannelSegmentFilterOrder order) {
        this.order = order;
    }

    @Override
    public ChannelSegmentFilterOrder getOrder() {
        return order;
    }

}
