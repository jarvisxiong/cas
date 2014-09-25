package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Marker;

import javax.inject.Singleton;

@Singleton
public class AuctionSeatIdFilter extends AbstractAuctionFilter {

    @Inject
    public AuctionSeatIdFilter(Provider<Marker> traceMarkerProvider,final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInRtbSeatidMisMatchFilter, serverConfiguration);
        isApplicableRTBD = true;
        isApplicableIX   = false;
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (rtbSegment.getChannelEntity().getAccountId()
                .equalsIgnoreCase(rtbSegment.getAdNetworkInterface().getSeatId())) {
            return false;
        }
        return true;
    }
}
