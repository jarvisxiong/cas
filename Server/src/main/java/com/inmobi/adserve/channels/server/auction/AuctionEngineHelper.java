package com.inmobi.adserve.channels.server.auction;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;

/**
 * Created by ishan.bhatnagar on 9/3/15.
 */
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
     * Returns the updated unfiltpered channel segment list.
     * All channel segments for which DSP info is available are updated to their DSP equivalent versions.
     *
     *  @param unfilteredChannelSegmentList
     * @param filteredChannelSegmentList
     * @return
     */
    static List<ChannelSegment> mapRPChannelSegmentsToDSPChannelSegments(final List<ChannelSegment>
            unfilteredChannelSegmentList, final List<ChannelSegment> filteredChannelSegmentList) {

        /*
            Revert on upgrade to Guice 4.0
            return Stream.concat(unfilteredChannelSegmentList.stream()
                .filter(channelSegment -> filteredChannelSegmentList.contains(channelSegment))
                .map(channelSegment -> AuctionEngineHelper
                    .updateChannelSegmentWithDSPFields(channelSegment)), unfilteredChannelSegmentList.stream()
                .filter(channelSegment -> !filteredChannelSegmentList.contains(channelSegment)))
                .collect(Collectors.toList());
        */

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
}
