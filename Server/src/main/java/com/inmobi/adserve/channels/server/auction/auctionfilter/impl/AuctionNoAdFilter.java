package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuctionNoAdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionNoAdFilter(Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, null);
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (rtbSegment.getAdNetworkInterface().getAdStatus().equalsIgnoreCase("AD")) {
            return false;
        }
        return true;
    }
}
