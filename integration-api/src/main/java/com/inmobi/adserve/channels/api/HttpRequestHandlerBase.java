package com.inmobi.adserve.channels.api;

import io.netty.channel.Channel;

import com.ning.http.client.AsyncHttpClient;


public abstract class HttpRequestHandlerBase {

    public abstract void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final Channel channel);

    public abstract void sendNoAdResponse(final Channel channel);

    public abstract Boolean isEligibleForProcess(final AdNetworkInterface adNetwork);

    public abstract Boolean isLastEntry(final AdNetworkInterface adNetwork);

    public abstract void reassignRanks(final AdNetworkInterface adNetwork, final Channel channel);

    public abstract void cleanUp();

    public abstract void processDcpList(final Channel channel);

    public abstract void processDcpPartner(final Channel channel, final AdNetworkInterface adNetworkInterface);

    public abstract AuctionEngineInterface getAuctionEngine();

    public abstract AsyncHttpClient getAsyncClient();
}
