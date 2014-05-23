package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuctionCurrencyFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionCurrencyFilter(Provider<Marker> traceMarkerProvider,final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInRtbCurrencyNotSupportedFilter, serverConfiguration);
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (RtbAdNetwork.getCurrenciesSupported().contains(
                rtbSegment.getAdNetworkInterface().getCurrency())) {
           return false;
        }
        return true;
    }
}


