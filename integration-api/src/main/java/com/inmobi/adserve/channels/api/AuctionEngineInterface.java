package com.inmobi.adserve.channels.api;

public interface AuctionEngineInterface {
    boolean isAllRtbComplete();

    double getSecondBidPrice();

    AdNetworkInterface runRtbSecondPriceAuctionEngine();

    boolean isAuctionComplete();

    boolean isRtbResponseNull();

}
