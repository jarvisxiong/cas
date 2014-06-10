package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuctionCreativeIdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionCreativeIdFilter(Provider<Marker> traceMarkerProvider,final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInCreativeIdMissingFilter, serverConfiguration);
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (StringUtils.isEmpty(rtbSegment.getAdNetworkInterface().getCreativeId())) {
            return true;
        }
        return false;
    }

}
