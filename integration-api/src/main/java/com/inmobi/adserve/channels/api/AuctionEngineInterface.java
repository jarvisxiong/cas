package com.inmobi.adserve.channels.api;

public interface AuctionEngineInterface {
    boolean areAllChannelSegmentRequestsComplete();

    AdNetworkInterface runAuctionEngine();

    boolean isAuctionComplete();

    boolean isAuctionResponseNull();
}
