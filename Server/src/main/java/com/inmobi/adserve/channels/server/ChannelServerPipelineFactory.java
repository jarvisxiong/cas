package com.inmobi.adserve.channels.server;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.handler.NettyRequestScopeSeedHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

@Singleton
public class ChannelServerPipelineFactory extends ChannelInitializer<SocketChannel> {
    private final RequestIdHandler requestIdHandler;
    private final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler;
    private final ConnectionLimitHandler incomingConnectionLimitHandler;
    private final ServerConfig serverConfig;
    private final RequestParserHandler requestParserHandler;

    @Inject
    ChannelServerPipelineFactory(final ServerConfig serverConfig,
            final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler,
            final ConnectionLimitHandler incomingConnectionLimitHandler, final RequestParserHandler requestParserHandler) {
        this.serverConfig = serverConfig;
        this.nettyRequestScopeSeedHandler = nettyRequestScopeSeedHandler;
        requestIdHandler = new RequestIdHandler();
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
        this.requestParserHandler = requestParserHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        // enable logging handler only for dev purpose
        // pipeline.addLast("logging", loggingHandler);
        pipeline.addLast("incomingLimitHandler", incomingConnectionLimitHandler);
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        // 1 MB max request size
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("casTimeoutHandler", new CasTimeoutHandler(serverConfig));
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("nettyRequestScopeSeedHandler", nettyRequestScopeSeedHandler);
        pipeline.addLast("requestParserHandler", requestParserHandler);
    }
}
