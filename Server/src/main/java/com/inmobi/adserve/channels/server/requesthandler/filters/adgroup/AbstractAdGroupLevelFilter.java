package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class AbstractAdGroupLevelFilter implements AdGroupLevelFilter {
    protected final Provider<Marker> traceMarkerProvider;
    private final String inspectorString;
    private FilterOrder order;
    private final String className;

    protected AbstractAdGroupLevelFilter(final Provider<Marker> traceMarkerProvider, final String inspectorString) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.inspectorString = inspectorString;
        this.className = this.getClass().getSimpleName();
    }

    @Override
    public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
            final CasContext casContext) {
        final Marker traceMarker = traceMarkerProvider.get();
        for (final Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
            final ChannelSegment channelSegment = iterator.next();
            final boolean result = failedInFilter(channelSegment, sasParams, casContext);
            final String adgroupId = channelSegment.getChannelSegmentEntity().getAdgroupId();
            final String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
            if (result) {
                iterator.remove();
                log.debug(traceMarker, "Failed in filter: {}, adgroup: {}, advertiser: {}",
                        className, adgroupId, advertiserId);
                incrementStats(channelSegment);
            } else {
                log.debug(traceMarker, "Passed in filter: {}, adgroup: {}, advertiser: {}",
                        className, adgroupId, advertiserId);
            }
        }
    }

    protected void incrementStats(final ChannelSegment channelSegment) {
        channelSegment.incrementInspectorStats(inspectorString);
    }

    protected abstract boolean failedInFilter(final ChannelSegment channelSegment,
            final SASRequestParameters sasParams, final CasContext casContext);

    @Override
    final public void setOrder(final FilterOrder order) {
        this.order = order;
    }

    @Override
    public FilterOrder getOrder() {
        return order;
    }

}
