package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Iterator;
import java.util.List;


/**
 * @author abhishek.parwal
 * 
 */
public abstract class AbstractAdvertiserLevelFilter implements AdvertiserLevelFilter {

    private static final Logger       LOG = LoggerFactory.getLogger(AbstractAdvertiserLevelFilter.class);

    private final Provider<Marker>    traceMarkerProvider;

    private final String              inspectorString;

    private FilterOrder order;

    protected AbstractAdvertiserLevelFilter(final Provider<Marker> traceMarkerProvider, final String inspectorString) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.inspectorString = inspectorString;
    }

    @Override
    public final void filter(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails,
            final SASRequestParameters sasParams) {

        Marker traceMarker = traceMarkerProvider.get();

        for (Iterator<AdvertiserMatchedSegmentDetail> iterator = matchedSegmentDetails.iterator(); iterator.hasNext();) {
            AdvertiserMatchedSegmentDetail matchedSegmentDetail = iterator.next();

            /*
             * All the Advertiser Level filters (extending this abstract class) are on advertiser level properties.
             * The filer is applied only on the first channelSegment in the ChannelSegmentList for an advertiser. Being a
             * filter on advertiser level properties, the filtering result is expected to be same for all segments.
             */
            ChannelSegment channelSegment = matchedSegmentDetail.getChannelSegmentList().get(0);

            boolean result = failedInFilter(channelSegment, sasParams);

            String advertiserId = channelSegment.getChannelEntity().getAccountId();

            if (result) {
                iterator.remove();
                LOG.debug(traceMarker, "Failed in filter {}  , advertiser {}", this.getClass().getSimpleName(),
                        advertiserId);
                incrementStats(matchedSegmentDetail.getChannelSegmentList());
            } else {
                LOG.debug(traceMarker, "Passed in filter {} ,  advertiser {}", this.getClass().getSimpleName(),
                        advertiserId);
            }
        }
    }

    /**
     * @param channelSegments
     */
    protected void incrementStats(final List<ChannelSegment> channelSegments) {
        channelSegments.get(0).incrementInspectorStats(inspectorString, channelSegments.size());
    }

    /**
     * @param channelSegment
     * @param sasParams
     * @return {@link boolean}
     */
    protected abstract boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams);

    @Override
    final public void setOrder(final FilterOrder order) {
        this.order = order;
    }

    @Override
    public FilterOrder getOrder() {
        return order;
    }
}