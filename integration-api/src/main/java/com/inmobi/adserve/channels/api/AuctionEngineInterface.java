package com.inmobi.adserve.channels.api;


import com.inmobi.adserve.channels.repository.RepositoryHelper;

public interface AuctionEngineInterface {
    boolean areAllChannelSegmentRequestsComplete();

    double getSecondBidPrice();

    AdNetworkInterface runAuctionEngine();

    boolean isAuctionComplete();

    boolean isAuctionResponseNull();

    public boolean updateDSPAccountInfo(RepositoryHelper repositoryHelper, String buyer);
}
