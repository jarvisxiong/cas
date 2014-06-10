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
public class AuctionCreativeAttributeFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionCreativeAttributeFilter(Provider<Marker> traceMarkerProvider,final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInCreativeAttributesMissingFilter, serverConfiguration);
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (null == rtbSegment.getAdNetworkInterface().getAttribute() || rtbSegment.getAdNetworkInterface().getAttribute().isEmpty()) {
            return true;
        }
        return false;
    }
}
