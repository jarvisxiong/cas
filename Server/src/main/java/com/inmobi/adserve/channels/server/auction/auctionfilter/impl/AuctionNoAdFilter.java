package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuctionNoAdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionNoAdFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, null, serverConfiguration);
        isApplicableRTBD = true;
        isApplicableIX = true;
        isApplicableHosted = true;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment rtbSegment,
            final CasInternalRequestParameters casInternalRequestParameters) {
        if ("AD".equalsIgnoreCase(rtbSegment.getAdNetworkInterface().getAdStatus())) {
            return false;
        }
        return true;
    }
}
