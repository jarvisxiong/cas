package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import javax.inject.Inject;

import lombok.Getter;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.handler.NettyRequestScopeSeedHandler;

public class ChannelStatServerPipelineFactory extends ChannelInitializer<SocketChannel> {

    private final RequestIdHandler requestIdHandler;
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler;
    @Getter
    private final CasConfigUtil servletHandler;
    private final RequestParserHandler requestParserHandler;
    private final ServerConfig serverConfig;

    @Inject
    ChannelStatServerPipelineFactory(final ServerConfig serverConfig,
            final Provider<HttpRequestHandler> httpRequestHandlerProvider, final CasConfigUtil servletHandler,
            final NettyRequestScopeSeedHandler nettyRequestScopeSeedHandler,
            final RequestParserHandler requestParserHandler) {

        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
        this.nettyRequestScopeSeedHandler = nettyRequestScopeSeedHandler;
        requestIdHandler = new RequestIdHandler();
        this.servletHandler = servletHandler;
        this.requestParserHandler = requestParserHandler;
        this.serverConfig = serverConfig;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));// 1
        // MB
        // data
        // size
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("nettyRequestScopeSeedHandler", nettyRequestScopeSeedHandler);
        pipeline.addLast("requestParserHandler", requestParserHandler);
    }

}
