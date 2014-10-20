package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;

@Singleton
public class AuctionAdvertiserDomainFilter extends AbstractAuctionFilter {

	@Inject
	protected AuctionAdvertiserDomainFilter(final Provider<Marker> traceMarkerProvider,
			final ServerConfig serverConfiguration) {
		super(traceMarkerProvider, InspectorStrings.DROPPED_IN_ADVERTISER_DOMAINS_MISSING_FILTER, serverConfiguration);
		isApplicableRTBD = true;
		isApplicableIX = false;
	}

	@Override
	protected boolean failedInFilter(final ChannelSegment rtbSegment,
			final CasInternalRequestParameters casInternalRequestParameters) {
		return null == rtbSegment.getAdNetworkInterface().getADomain()
				|| rtbSegment.getAdNetworkInterface().getADomain().isEmpty();
	}
}
