package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.Getter;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.config.ServerConfig;
import com.inmobi.adserve.channels.server.handler.TraceMarkerhandler;
import com.inmobi.adserve.channels.util.annotations.IncomingConnectionLimitHandler;


@Singleton
public class ChannelServerPipelineFactory extends ChannelInitializer<SocketChannel> {

    private final RequestIdHandler             requestIdHandler;
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final TraceMarkerhandler           traceMarkerhandler;
    @Getter
    private final ConnectionLimitHandler       incomingConnectionLimitHandler;
    private final ServletHandler               servletHandler;
    private final ServerConfig                 serverConfig;
    private final LoggingHandler               loggingHandler;

    @Inject
    ChannelServerPipelineFactory(final ServerConfig serverConfig,
            final Provider<HttpRequestHandler> httpRequestHandlerProvider, final TraceMarkerhandler traceMarkerhandler,
            final ServletHandler servletHandler,
            @IncomingConnectionLimitHandler final ConnectionLimitHandler incomingConnectionLimitHandler,
            final LoggingHandler loggingHandler) {

        this.serverConfig = serverConfig;
        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
        this.traceMarkerhandler = traceMarkerhandler;
        this.requestIdHandler = new RequestIdHandler();
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
        this.servletHandler = servletHandler;
        this.loggingHandler = loggingHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // enable logging handler only for dev purpose
        pipeline.addLast("logging", loggingHandler);
        pipeline.addLast("incomingLimitHandler", incomingConnectionLimitHandler);
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));// 1 MB data size
        pipeline.addLast("requestIdHandler", requestIdHandler);
        // pipeline.addLast("casWriteTimeoutHandler", new
        // CasWriteTimeOutHandler(serverConfig.getServerTimeoutInMillis(),
        // TimeUnit.MILLISECONDS));
        pipeline.addLast("casWriteTimeoutHandler", new WriteTimeoutHandler(serverConfig.getServerTimeoutInMillis(),
                TimeUnit.MILLISECONDS));
        pipeline.addLast("traceMarkerhandler", traceMarkerhandler);
        pipeline.addLast("servletHandler", servletHandler);
        // pipeline.addLast("casCodec", new CasCodec());
        pipeline.addLast("handler", httpRequestHandlerProvider.get());
    }
}
