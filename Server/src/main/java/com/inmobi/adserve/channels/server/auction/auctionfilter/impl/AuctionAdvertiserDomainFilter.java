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
public class AuctionAdvertiserDomainFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionAdvertiserDomainFilter(Provider<Marker> traceMarkerProvider,final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInAdvertiserDomainsMissingFilter, serverConfiguration);
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        return null == rtbSegment.getAdNetworkInterface().getADomain() || rtbSegment.getAdNetworkInterface().getADomain().isEmpty();
    }
}
