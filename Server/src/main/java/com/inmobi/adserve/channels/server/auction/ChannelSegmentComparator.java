package com.inmobi.adserve.channels.server.auction;

import java.util.Comparator;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

/**
 * TODO: Add comment here
 */
final class ChannelSegmentComparator implements Comparator<ChannelSegment> {

    @Override
    public int compare(final ChannelSegment a1, final ChannelSegment a2) {
        int outcome;
        final AdNetworkInterface adNetwork1 = a1.getAdNetworkInterface();
        final AdNetworkInterface adNetwork2 = a2.getAdNetworkInterface();

        final DealEntity bidder1Deal  = adNetwork1.getDeal();
        final DealEntity bidder2Deal  = adNetwork2.getDeal();
        final boolean bidder1HasTrumpDeal = null != bidder1Deal && bidder1Deal.isTrumpDeal();
        final boolean bidder2HasTrumpDeal = null != bidder2Deal && bidder2Deal.isTrumpDeal();

        final Double effectiveBid1 = adNetwork1.getBidPriceInUsd();
        final Double effectiveBid2 = adNetwork2.getBidPriceInUsd();
        final Long latency1 = adNetwork1.getLatency();
        final Long latency2 = adNetwork2.getLatency();

        if (bidder1HasTrumpDeal) {
            if (bidder2HasTrumpDeal) {
                outcome = effectiveBid2.compareTo(effectiveBid1);
                if (0 == outcome) {
                    outcome = latency1.compareTo(latency2);
                }
            } else {
                outcome = -1;
            }
        } else {
            if (bidder2HasTrumpDeal) {
                outcome = 1;
            } else {
                outcome = effectiveBid2.compareTo(effectiveBid1);
                if (0 == outcome) {
                    outcome = latency1.compareTo(latency2);
                }
            }
        }

        return outcome;
    }

}
