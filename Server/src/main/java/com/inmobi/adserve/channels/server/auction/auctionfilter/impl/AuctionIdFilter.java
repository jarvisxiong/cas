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
public class AuctionIdFilter extends AbstractAuctionFilter {

  @Inject
  protected AuctionIdFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
    super(traceMarkerProvider, InspectorStrings.DROPPED_IN_RTB_AUCTION_ID_MIS_MATCH_FILTER, serverConfiguration);
    isApplicableRTBD = true;
    isApplicableIX = true;
  }

  @Override
  protected boolean failedInFilter(final ChannelSegment rtbSegment,
      final CasInternalRequestParameters casInternalRequestParameters) {
    if (casInternalRequestParameters.getAuctionId().equalsIgnoreCase(rtbSegment.getAdNetworkInterface().getAuctionId())) {
      return false;
    }
    return true;
  }
}
