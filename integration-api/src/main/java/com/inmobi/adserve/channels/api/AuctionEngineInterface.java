package com.inmobi.adserve.channels.api;


public interface AuctionEngineInterface {
    boolean isAuctionAllComplete();

    double getSecondBidPrice();

    AdNetworkInterface runAuctionEngine();

    boolean isAuctionComplete();

    boolean isAuctionResponseNull();

}
