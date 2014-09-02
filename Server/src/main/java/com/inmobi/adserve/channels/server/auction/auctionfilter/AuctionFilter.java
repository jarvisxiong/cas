package com.inmobi.adserve.channels.server.auction.auctionfilter;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;

import java.util.List;

public interface AuctionFilter extends ChannelSegmentFilter {

    /**
     * @param channelSegments
     * @param casInternalRequestParameters
     */
    void filter(final List<ChannelSegment> channelSegments, final CasInternalRequestParameters casInternalRequestParameters);

    boolean isApplicable(final String advertiserId);

    boolean isApplicable(final int dst);

}