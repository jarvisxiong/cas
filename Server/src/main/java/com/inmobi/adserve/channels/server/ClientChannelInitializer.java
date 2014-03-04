package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.config.ServerConfig;


/**
 * @author abhishek.parwal
 * 
 */
public abstract class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelsClientHandler    channelHandler;
    protected final ConnectionLimitHandler connectionLimitUpstreamHandler;
    private final ServerConfig             serverConfig;

    protected ClientChannelInitializer(final ServerConfig serverConfig, final ConnectionType connectionType) {
        channelHandler = new ChannelsClientHandler();
        connectionLimitUpstreamHandler = new ConnectionLimitHandler(serverConfig, connectionType);
        this.serverConfig = serverConfig;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("connectionLimit", connectionLimitUpstreamHandler);
        pipeline.addLast("timeout", new ReadTimeoutHandler(serverConfig.getDcpRequestTimeoutInMillis(),
                TimeUnit.MILLISECONDS));
        pipeline.addLast("httpClientCodec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("handler", channelHandler);

    }
}
