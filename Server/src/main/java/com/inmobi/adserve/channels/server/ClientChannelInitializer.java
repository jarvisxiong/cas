package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LoggingHandler;
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
    private final LoggingHandler           loggingHandler;

    protected ClientChannelInitializer(final ServerConfig serverConfig, final ConnectionType connectionType,
            final LoggingHandler loggingHandler) {
        channelHandler = new ChannelsClientHandler();
        connectionLimitUpstreamHandler = new ConnectionLimitHandler(serverConfig, connectionType);
        this.serverConfig = serverConfig;
        this.loggingHandler = loggingHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("logging", loggingHandler);
        pipeline.addLast("connectionLimit", connectionLimitUpstreamHandler);
        pipeline.addLast("timeout", new ReadTimeoutHandler(serverConfig.getDcpRequestTimeoutInMillis(),
                TimeUnit.MILLISECONDS));
        pipeline.addLast("httpClientCodec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("handler", channelHandler);

    }
}
