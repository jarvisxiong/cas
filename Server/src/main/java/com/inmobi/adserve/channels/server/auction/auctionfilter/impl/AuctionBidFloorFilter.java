package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;

@Singleton
public class AuctionBidFloorFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionBidFloorFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_RTB_BID_FLOOR_FILTER, serverConfiguration);
        isApplicableRTBD = true;
        isApplicableIX = true;
    }

    /**
     * This filter fails channelSegments which have bid lower than the effective auction bid floor.<br>
     * For RTBD, the effective auction bid floor is set to<br>
     * max(casParams.getAuctionBidFloor(), demandDensity (=alpha*omega))<br>
     * otherwise, it is set to<br>
     * casParams.getAuctionBidFloor()
     *
     * @param rtbSegment
     * @param casInternalRequestParameters
     * @return
     */
    @Override
    protected boolean failedInFilter(final ChannelSegment rtbSegment,
            final CasInternalRequestParameters casInternalRequestParameters) {

        double effectiveAuctionBidFloor = casInternalRequestParameters.getAuctionBidFloor();
        if (rtbSegment.getAdNetworkInterface() instanceof RtbAdNetwork) {
            effectiveAuctionBidFloor =
                    Math.max(effectiveAuctionBidFloor, casInternalRequestParameters.getDemandDensity());
        }

        if (rtbSegment.getAdNetworkInterface().getBidPriceInUsd() < effectiveAuctionBidFloor) {
            return true;
        }
        return false;
    }
}
