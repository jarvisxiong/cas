package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AuctionDealFloorFilter extends AbstractAuctionFilter {
    private static final double ZERO = 0d;

    @Inject
    protected AuctionDealFloorFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_DEAL_FLOOR_FILTER, serverConfiguration);
        isApplicableRTBD = false;
        isApplicableIX = false;
    }

    // TODO: Should data vendor cost be consumed?
    @Override
    protected boolean failedInFilter(final ChannelSegment segment, final CasInternalRequestParameters casParams) {

        final AdNetworkInterface adn = segment.getAdNetworkInterface();
        final DealEntity deal = adn.getDeal();

        final Double bid = adn instanceof IXAdNetwork ?
                ((IXAdNetwork)adn).getOriginalBidPriceInUsd() : adn.getBidPriceInUsd();

        final double dealFloorInUSD = null != deal ?
                CasConfigUtil.repositoryHelper.calculatePriceInUSD(deal.getFloor(), deal.getCurrency()) : ZERO;

        return bid < dealFloorInUSD;
    }

}
