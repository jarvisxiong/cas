package com.inmobi.adserve.channels.api;

public interface AuctionEngineInterface {
    boolean areAllChannelSegmentRequestsComplete();

    double getSecondBidPrice();

    AdNetworkInterface runAuctionEngine();

    boolean isAuctionComplete();

    boolean isAuctionResponseNull();
}
