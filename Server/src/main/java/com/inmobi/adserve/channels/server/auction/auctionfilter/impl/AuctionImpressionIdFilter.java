package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;

@Singleton
public class AuctionImpressionIdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionImpressionIdFilter(final Provider<Marker> traceMarkerProvider,
            final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_RTB_IMPRESSION_ID_MIS_MATCH_FILTER, serverConfiguration);
        isApplicableRTBD = true;
        isApplicableIX = false;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment rtbSegment,
                                     final CasInternalRequestParameters casInternalRequestParameters) {
        if (rtbSegment.getAdNetworkInterface().getImpressionId()
                .equalsIgnoreCase(rtbSegment.getAdNetworkInterface().getRtbImpressionId())) {
            return false;
        }
        return true;
    }
}
