package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.adnetworks.mvp.HostedAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Marker;

import javax.inject.Inject;

/**
 * Created by ishanbhatnagar on 9/12/14.
 */
public class AuctionHostedIdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionHostedIdFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_HOSTED_AUCTION_ID_FILTER, serverConfiguration);
        isApplicableRTBD = false;
        isApplicableIX = false;
        isApplicableHosted = true;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment rtbSegment,
                                     final CasInternalRequestParameters casInternalRequestParameters) {
        HostedAdNetwork hostedAdNetwork = (HostedAdNetwork) rtbSegment.getAdNetworkInterface();

        if (hostedAdNetwork.getRequestId().equals(hostedAdNetwork.getResponseId())) {
            return false;
        }
        return true;
    }
}
