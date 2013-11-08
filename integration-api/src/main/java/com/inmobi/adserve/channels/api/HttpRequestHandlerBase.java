package com.inmobi.adserve.channels.api;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;

import com.ning.http.client.AsyncHttpClient;


public abstract class HttpRequestHandlerBase extends IdleStateAwareChannelUpstreamHandler
{

    public abstract void sendAdResponse(AdNetworkInterface selectedAdNetwork, ChannelEvent event);

    public abstract void sendNoAdResponse(ChannelEvent event);

    public abstract Boolean isEligibleForProcess(AdNetworkInterface adNetwork);

    public abstract Boolean isLastEntry(AdNetworkInterface adNetwork);

    public abstract void reassignRanks(AdNetworkInterface adNetwork, MessageEvent event);

    public abstract void cleanUp();

    public abstract void processDcpList(MessageEvent serverEvent);

    public abstract void processDcpPartner(MessageEvent serverEvent, AdNetworkInterface adNetworkInterface);

    public abstract AuctionEngineInterface getAuctionEngine();

    public abstract AsyncHttpClient getAsyncClient();
}
