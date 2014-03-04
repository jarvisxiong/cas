package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.Getter;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.server.annotations.IncomingConnectionLimitHandler;
import com.inmobi.adserve.channels.server.config.ServerConfig;
import com.inmobi.adserve.channels.server.handler.TraceMarkerhandler;


public class ChannelServerPipelineFactory extends ChannelInitializer<SocketChannel> {

    private final RequestIdHandler             requestIdHandler;
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final TraceMarkerhandler           traceMarkerhandler;
    @Getter
    private final ConnectionLimitHandler       incomingConnectionLimitHandler;
    private final ServletHandler               servletHandler;
    private final ServerConfig                 serverConfig;

    @Inject
    ChannelServerPipelineFactory(final ServerConfig serverConfig,
            final Provider<HttpRequestHandler> httpRequestHandlerProvider, final TraceMarkerhandler traceMarkerhandler,
            final ServletHandler servletHandler,
            @IncomingConnectionLimitHandler final ConnectionLimitHandler incomingConnectionLimitHandler) {

        this.serverConfig = serverConfig;

        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
        this.traceMarkerhandler = traceMarkerhandler;
        this.requestIdHandler = new RequestIdHandler();
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
        this.servletHandler = servletHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("incomingLimitHandler", incomingConnectionLimitHandler);
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));// 1 MB data size
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, serverConfig.getServerTimeoutInMillis(), 0,
                TimeUnit.MILLISECONDS));
        pipeline.addLast("traceMarkerhandler", traceMarkerhandler);
        pipeline.addLast("servletHandler", servletHandler);
        pipeline.addLast("handler", httpRequestHandlerProvider.get());
    }
}
