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

@Singleton
public class AuctionDealFloorFilter extends AbstractAuctionFilter {
    private static final double ZERO = 0d;

    @Inject
    protected AuctionDealFloorFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_DEAL_FLOOR_FILTER, serverConfiguration);
        isApplicableRTBD = true;
        isApplicableIX = true;
    }

    // TODO: Should data vendor cost be consumed?
    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final CasInternalRequestParameters casInternal) {

        final AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();
        final DealEntity deal = adNetwork.getDeal();

        final Double discount = adNetwork instanceof IXAdNetwork ?
                CasConfigUtil.getAdapterConfig().getDouble("ix.rubiconCutsInDeal", 0.0) : 0.0;

        final double dealFloorInUSD;
        if (null != deal) {
            dealFloorInUSD = CasConfigUtil.repositoryHelper.calculatePriceInUSD(deal.getFloor(), deal.getCurrency());
        } else {
            dealFloorInUSD = ZERO;
        }
        return adNetwork.getBidPriceInUsd() < dealFloorInUSD * (1 - discount);
    }

}
