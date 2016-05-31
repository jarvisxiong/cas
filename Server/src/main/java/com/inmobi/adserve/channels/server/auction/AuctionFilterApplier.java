package com.inmobi.adserve.channels.server.auction;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

@Singleton
public final class AuctionFilterApplier {

    private final List<AuctionFilter> auctionFilters;

    @Inject
    public AuctionFilterApplier(final List<AuctionFilter> auctionFilters) {
        this.auctionFilters = auctionFilters;
    }

    List<ChannelSegment> applyFilters(final List<ChannelSegment> segments, final CasInternalRequestParameters casParams) {
        for (final AuctionFilter auctionFilter : auctionFilters) {
            if (CollectionUtils.isEmpty(segments)) {
                break;
            }

            // Assuming that the dst of all segments is the same
            final boolean isFilterApplicable = auctionFilter.isApplicable(segments.get(0).getAdNetworkInterface());

            if (isFilterApplicable) {
                auctionFilter.filter(segments, casParams);
            }
        }
        return segments;
    }
}
