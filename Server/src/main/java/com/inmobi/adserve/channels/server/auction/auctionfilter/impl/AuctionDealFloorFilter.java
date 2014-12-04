package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;

@Singleton
public class AuctionDealFloorFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionDealFloorFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_DEAL_FLOOR_FILTER, serverConfiguration);
        isApplicableRTBD = false;
        isApplicableIX = false;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment rtbSegment,
                                     final CasInternalRequestParameters casInternalRequestParameters) {
        if (rtbSegment.getAdNetworkInterface() instanceof IXAdNetwork) {
            final IXAdNetwork ixAdNetwork = (IXAdNetwork) rtbSegment.getAdNetworkInterface();
            if (ixAdNetwork.isExternalPersonaDeal()) {
                if ((ixAdNetwork.getBidPriceInUsd()
                        < casInternalRequestParameters.getSiteFloor() + ixAdNetwork.returnDataVendorCost())
                        || (ixAdNetwork.getBidPriceInUsd() < ixAdNetwork.returndealFloor())) {
                    return true;
                }
            }
        }
        return false;
    }
}
