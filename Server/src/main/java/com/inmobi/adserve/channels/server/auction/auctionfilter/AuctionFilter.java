package com.inmobi.adserve.channels.server.auction.auctionfilter;

import java.util.List;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;

public interface AuctionFilter extends ChannelSegmentFilter {

    /**
     * @param channelSegments
     * @param casInternalRequestParameters
     */
    void filter(final List<ChannelSegment> channelSegments,
            final CasInternalRequestParameters casInternalRequestParameters);

    /**
     * 
     * @param advertiserId
     * @return
     */
    boolean isApplicable(final String advertiserId);

    /**
     * 
     * @param adNetworkInterface
     * @return
     */
    boolean isApplicable(final AdNetworkInterface adNetworkInterface);

}
