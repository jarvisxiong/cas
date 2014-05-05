package com.inmobi.adserve.channels.server.auction;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

import javax.inject.Inject;
import java.util.List;

@Singleton
public class AuctionFilterApplier {

    private final List<AuctionFilter> auctionFilters;

    @Inject
    public AuctionFilterApplier(final List<AuctionFilter> auctionFilters) {
        this.auctionFilters = auctionFilters;
    }

    public List<ChannelSegment> applyFilters(final List<ChannelSegment> rtbdSegments, final CasInternalRequestParameters casInternalRequestParameters) {
        for (AuctionFilter auctionFilter : auctionFilters) {
            auctionFilter.filter(rtbdSegments, casInternalRequestParameters);
        }
        return rtbdSegments;
    }
}
