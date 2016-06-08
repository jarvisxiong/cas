package com.inmobi.adserve.channels.server.auction.auctionfilter;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.casthrift.DemandSourceType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuctionFilter implements AuctionFilter {

    protected final Provider<Marker> traceMarkerProvider;
    protected Boolean isApplicableRTBD; // Whether the filter is applicable to RTBD
    protected Boolean isApplicableIX; // Whether the filter is applicable to IX

    private final String inspectorString;
    private FilterOrder order;
    private final ServerConfig serverConfiguration;
    private final String className;

    protected AbstractAuctionFilter(final Provider<Marker> traceMarkerProvider, final String inspectorString,
            final ServerConfig serverConfiguration) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.inspectorString = inspectorString;
        this.serverConfiguration = serverConfiguration;
        this.className = this.getClass().getSimpleName();
    }

    @Override
    public void filter(final List<ChannelSegment> channelSegments,
            final CasInternalRequestParameters casInternalRequestParameters) {
        Marker traceMarker = null; 
        for (final Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
            final ChannelSegment channelSegment = iterator.next();

            boolean result = false;

            // Check whether the auction filter is applicable to the particular channel entity and also whether it
            // is applicable to the particular demand source type
            if (isApplicable(channelSegment.getChannelEntity().getAccountId())) {
                result = failedInFilter(channelSegment, casInternalRequestParameters);
            }

            if (result) {
                iterator.remove();
                log.debug(traceMarker, "Failed in auction filter {}, advertiser {}", className,
                        channelSegment.getAdNetworkInterface().getName());
                incrementStats(channelSegment);
            } else {
                log.debug(traceMarker, "Passed in auction filter {}, advertiser {}", className,
                        channelSegment.getAdNetworkInterface().getName());
            }
        }
    }

    /**
     * @param channelSegment
     * @return
     */
    protected abstract boolean failedInFilter(final ChannelSegment channelSegment,
            final CasInternalRequestParameters casInternalRequestParameters);

    /**
     * @param channelSegment
     */
    protected void incrementStats(final ChannelSegment channelSegment) {
        if (StringUtils.isNotEmpty(inspectorString)) {
            channelSegment.incrementInspectorStats(inspectorString);
        }
    }

    @Override
    final public void setOrder(final FilterOrder order) {
        this.order = order;
    }

    @Override
    public FilterOrder getOrder() {
        return order;
    }

    @Override
    public boolean isApplicable(final String advertiserId) {
        return !serverConfiguration.getExcludedAdvertisers(className).contains(advertiserId);
    }

    @Override
    public boolean isApplicable(final AdNetworkInterface adNetworkInterface) {
        DemandSourceType dst = adNetworkInterface.getDst();
        switch (dst) {
            case RTBD:
                return isApplicableRTBD;
            case IX:
                return isApplicableIX;
            default:
                return true;
        }
    }
}
