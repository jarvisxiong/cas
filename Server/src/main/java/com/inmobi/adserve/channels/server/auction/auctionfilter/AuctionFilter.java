package com.inmobi.adserve.channels.server.auction.auctionfilter;

import java.util.List;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;


public interface AuctionFilter extends ChannelSegmentFilter {

    void filter(final List<ChannelSegment> channelSegments,
            final CasInternalRequestParameters casInternalRequestParameters);

    boolean isApplicable(final String advertiserId);

    boolean isApplicable(final AdNetworkInterface adNetworkInterface);

}
