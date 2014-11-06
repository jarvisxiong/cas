package com.inmobi.adserve.channels.api;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

public interface AuctionEngineInterface {
    boolean areAllChannelSegmentRequestsComplete();

    double getSecondBidPrice();

    AdNetworkInterface runAuctionEngine();

    boolean isAuctionComplete();

    boolean isAuctionResponseNull();

    public void updateIXChannelSegment(final ChannelSegmentEntity dspChannelSegmentEntity);
}
