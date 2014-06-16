package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;

import javax.inject.Inject;

import lombok.Getter;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.handler.NettyRequestScopeSeedHandler;


@Singleton
public class ChannelServerPipelineFactory extends ChannelInitializer<SocketChannel> {

    private final RequestIdHandler             requestIdHandler;
    private final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler;
    @Getter
    private final ConnectionLimitHandler       incomingConnectionLimitHandler;
    private final CasConfigUtil                servletHandler;
    private final ServerConfig                 serverConfig;
    private final LoggingHandler               loggingHandler;
    private final RequestParserHandler         requestParserHandler;
    private final CasExceptionHandler          casExceptionHandler;

    @Inject
    ChannelServerPipelineFactory(final ServerConfig serverConfig,
            final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler, final CasConfigUtil servletHandler,
            final ConnectionLimitHandler incomingConnectionLimitHandler, final LoggingHandler loggingHandler,
            final RequestParserHandler requestParserHandler, final CasExceptionHandler casExceptionHandler) {

        this.serverConfig = serverConfig;
        this.nettyRequestScopeSeedHandler = nettyRequestScopeSeedHandler;
        this.requestIdHandler = new RequestIdHandler();
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
        this.servletHandler = servletHandler;
        this.loggingHandler = loggingHandler;
        this.requestParserHandler = requestParserHandler;
        this.casExceptionHandler = casExceptionHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // enable logging handler only for dev purpose
        // pipeline.addLast("logging", loggingHandler);
        pipeline.addLast("incomingLimitHandler", incomingConnectionLimitHandler);
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));// 1 MB data size
        pipeline.addLast("casWriteTimeoutHandler", new CasTimeoutHandler(serverConfig.getServerTimeoutInMillisForRTB(), serverConfig.getServerTimeoutInMillisForDCP()));
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("nettyRequestScopeSeedHandler", nettyRequestScopeSeedHandler);
        pipeline.addLast("requestParserHandler", requestParserHandler);
    }
}
