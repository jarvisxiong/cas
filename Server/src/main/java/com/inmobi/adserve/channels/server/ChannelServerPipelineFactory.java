package com.inmobi.adserve.channels.server;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

import com.google.inject.Injector;
import com.google.inject.Provider;


public class ChannelServerPipelineFactory implements ChannelPipelineFactory {

    private final Timer                        timer;
    private int                                serverTimeoutMillis;
    private final ExecutionHandler             executionHandler;
    private final RequestIdHandler             requestIdHandler;
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final TraceMarkerhandler           traceMarkerhandler;

    @Inject
    ChannelServerPipelineFactory(final Timer timer, @ServerConfiguration final Configuration configuration,
            final Provider<HttpRequestHandler> httpRequestHandlerProvider, final Injector injector) {
        this.timer = timer;
        try {
            this.serverTimeoutMillis = configuration.getInt("serverTimeoutMillis");
        }
        catch (Exception e) {
            this.serverTimeoutMillis = 825;
        }
        executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(80, 1048576, 1048576, 3,
                TimeUnit.HOURS));
        this.requestIdHandler = new RequestIdHandler();
        this.traceMarkerhandler = new TraceMarkerhandler();
        injector.injectMembers(traceMarkerhandler);
        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(requestIdHandler, new HttpRequestDecoder(), new HttpResponseEncoder(),
            new HttpChunkAggregator(100000000), executionHandler, new IdleStateHandler(this.timer, 0, 0,
                    serverTimeoutMillis, TimeUnit.MILLISECONDS), traceMarkerhandler, httpRequestHandlerProvider.get());
    }
}
