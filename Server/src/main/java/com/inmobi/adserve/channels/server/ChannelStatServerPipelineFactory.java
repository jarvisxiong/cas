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

import org.apache.commons.configuration.Configuration;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;
import com.inmobi.adserve.channels.server.handler.TraceMarkerhandler;


public class ChannelStatServerPipelineFactory extends ChannelInitializer<SocketChannel> {

    private final RequestIdHandler             requestIdHandler;
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final TraceMarkerhandler           traceMarkerhandler;
    private final int                          serverTimeoutMillis;
    @Getter
    private final ServletHandler               servletHandler;

    @Inject
    ChannelStatServerPipelineFactory(@ServerConfiguration final Configuration configuration,
            final Provider<HttpRequestHandler> httpRequestHandlerProvider, final ServletHandler servletHandler,
            final TraceMarkerhandler traceMarkerhandler) {
        this.serverTimeoutMillis = configuration.getInt("serverTimeoutMillis", 825);
        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
        this.traceMarkerhandler = traceMarkerhandler;
        this.requestIdHandler = new RequestIdHandler();
        this.servletHandler = servletHandler;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoderEncoder", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));// 1 MB data size
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, serverTimeoutMillis, 0, TimeUnit.MILLISECONDS));
        pipeline.addLast("traceMarkerhandler", traceMarkerhandler);
        pipeline.addLast("servletHandler", servletHandler);
        pipeline.addLast("handler", httpRequestHandlerProvider.get());
    }

}
