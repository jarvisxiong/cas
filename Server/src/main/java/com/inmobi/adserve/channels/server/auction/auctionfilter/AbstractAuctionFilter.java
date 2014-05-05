package com.inmobi.adserve.channels.server.auction.auctionfilter;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.constants.ChannelSegmentFilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractAuctionFilter implements AuctionFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuctionFilter.class);

    protected final Provider<Marker> traceMarkerProvider;
    private final String              inspectorString;
    private ChannelSegmentFilterOrder order;
    private ServerConfig serverConfiguration;


    protected AbstractAuctionFilter(final Provider<Marker> traceMarkerProvider, final String inspectorString, final ServerConfig serverConfiguration) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.inspectorString = inspectorString;
        this.serverConfiguration = serverConfiguration;
    }


    @Override
    public void filter(List<ChannelSegment> channelSegments, CasInternalRequestParameters casInternalRequestParameters) {
        Marker traceMarker = traceMarkerProvider.get();

        for (Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
            ChannelSegment channelSegment = iterator.next();

            boolean result = false;

            if (isApplicable(channelSegment.getChannelEntity().getAccountId())) {
                failedInFilter(channelSegment, casInternalRequestParameters);
            }

            if (result) {
                iterator.remove();
                LOG.debug(traceMarker, "Failed in auction filter {}  , advertiser {}", this.getClass().getSimpleName(),
                        channelSegment.getAdNetworkInterface().getName());
                incrementStats(channelSegment);
            }
            else {
                LOG.debug(traceMarker, "Passed in auction filter {} ,  advertiser {}", this.getClass().getSimpleName(),
                        channelSegment.getAdNetworkInterface().getName());
            }
        }
    }

    /**
     * @param channelSegment
     * @return
     */
    protected abstract boolean failedInFilter(final ChannelSegment channelSegment, final CasInternalRequestParameters casInternalRequestParameters);

    /**
     * @param channelSegment
     */
    protected void incrementStats(final ChannelSegment channelSegment) {
        if (StringUtils.isNotEmpty(inspectorString)) {
            channelSegment.incrementInspectorStats(inspectorString);
        }
    }

    @Override
    final public void setOrder(final ChannelSegmentFilterOrder order) {
        this.order = order;
    }

    @Override
    public ChannelSegmentFilterOrder getOrder() {
        return order;
    }

    @Override
    public boolean isApplicable(final String advertiserId) {
        return !this.serverConfiguration.getExcludedAdvertisers(this.getClass().getName()).contains(advertiserId);
    }
}
