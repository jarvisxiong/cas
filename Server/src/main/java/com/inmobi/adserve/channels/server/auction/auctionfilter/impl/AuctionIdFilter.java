package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuctionIdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionIdFilter(Provider<Marker> traceMarkerProvider,final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_RTB_AUCTION_ID_MIS_MATCH_FILTER, serverConfiguration);
        isApplicableRTBD = true;
        isApplicableIX = true;
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (casInternalRequestParameters.auctionId.equalsIgnoreCase(rtbSegment.getAdNetworkInterface()
                .getAuctionId())) {
            return false;
        }
        return true;
    }
}
