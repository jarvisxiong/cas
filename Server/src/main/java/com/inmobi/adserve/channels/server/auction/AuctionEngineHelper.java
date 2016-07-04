package com.inmobi.adserve.channels.server.auction;

import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

import lombok.NoArgsConstructor;


@NoArgsConstructor(access = PRIVATE)
class AuctionEngineHelper {
    /**
     * Update rp specific channel segments with DSP specific fields.
     * This is being done because we want all the logging to be done on the DSP details and not RP.
     */
     static ChannelSegment updateChannelSegmentWithDSPFields(final ChannelSegment rpChannelSegment) {
        return new ChannelSegment(((IXAdNetwork) rpChannelSegment.getAdNetworkInterface())
            .getDspChannelSegmentEntity(), null, null, null, null, rpChannelSegment.getAdNetworkInterface(), -1L);
    }

    /**
     * Returns the updated unfiltered channel segment list.
     * All channel segments for which DSP info is available are updated to their DSP equivalent versions.
     */
    static List<ChannelSegment> mapRPChannelSegmentsToDSPChannelSegments(final List<ChannelSegment>
            unfilteredChannelSegmentList, final List<ChannelSegment> filteredChannelSegmentList) {

        final List<ChannelSegment> updatedUnfilteredChannelSegmentList = new ArrayList<>();
        for (final ChannelSegment channelSegment : unfilteredChannelSegmentList) {
            if (filteredChannelSegmentList.contains(channelSegment)) {
                updatedUnfilteredChannelSegmentList.add(updateChannelSegmentWithDSPFields(channelSegment));
            } else {
                updatedUnfilteredChannelSegmentList.add(channelSegment);
            }
        }
        return updatedUnfilteredChannelSegmentList;
    }

    /**
     * Computes the clearing price for the auction.
     *
     * Assumptions: highestBid >= demandDensity (alpha*omega) [This is enforced in the Auction Bid Filter]
     * InmobiLongTermRevenue >= demandDensity [Enforced while initialising]
     *
     * Ask Price Range = [demandDensity, InmobiLongTermRevenue] (equivalent to [alpha*omega, beta*omega])
     *
     * The ask price range is divided into publisherYield(gamma) intervals, with the clearing price being the highest
     * ask price <= highest bid.
     *
     * Calculation Logic:
     *
     * Difference = (InmobiLongTermRevenue-demandDensity)/publisherYield (equivalent to omega*(beta-alpha)/gamma)
     *
     * If Difference != 0, index = min(Floor((HighestBid-demandDensity)/difference), publisherYield) (index is always >=
     * 0 as highestBid >= demandDensity) Clearing Price = difference*index + demandDensity Else Clearing Price =
     * demandDensity
     */
     static double getClearingPrice(final double highestBid, final CasInternalRequestParameters casParams) {
        final double demandDensity = casParams.getDemandDensity();
        final double longTermRevenue = casParams.getLongTermRevenue();
        final int publisherYield = casParams.getPublisherYield() >= 1 ? casParams.getPublisherYield() : 1; // Sanity

        final double diff = (longTermRevenue - demandDensity) / publisherYield;

        if (0 != diff) {
            final int index = Math.min((int) ((highestBid - demandDensity) / diff), publisherYield);
            return diff * index + demandDensity;
        } else {
            return demandDensity;
        }
    }

}
