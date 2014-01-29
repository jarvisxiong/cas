package com.inmobi.adserve.channels.server;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;
import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.handler.TraceMarkerhandler;
import lombok.Getter;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.jboss.netty.channel.Channels.pipeline;


public class ChannelServerPipelineFactory implements ChannelPipelineFactory {

    private final RequestIdHandler             requestIdHandler;
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final TraceMarkerhandler           traceMarkerhandler;
    private final Timer                        timer;
    private final int                          serverTimeoutMillis;
    private final ExecutionHandler             executionHandler;
    @Getter
    private final ConnectionLimitHandler       incomingConnectionLimitHandler;
    private final ServletHandler               servletHandler;

    @Inject
    ChannelServerPipelineFactory(final Timer timer, @ServerConfiguration final Configuration configuration,
            final Provider<HttpRequestHandler> httpRequestHandlerProvider, final TraceMarkerhandler traceMarkerhandler,
            final ServletHandler servletHandler) {
        this.timer = timer;
        this.serverTimeoutMillis = configuration.getInt("serverTimeoutMillis", 825);
        executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(80, 1048576, 1048576, 3,
                TimeUnit.HOURS));
        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
        this.traceMarkerhandler = traceMarkerhandler;
        this.requestIdHandler = new RequestIdHandler();
        this.incomingConnectionLimitHandler = new ConnectionLimitHandler(configuration, ConnectionType.INCOMING);
        this.servletHandler = servletHandler;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("requestIdHandler", requestIdHandler);
        pipeline.addLast("incomingLimitHandler", incomingConnectionLimitHandler);
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("httpchunkhandler", new HttpChunkAggregator(100000000));
        pipeline.addLast("executionHandler", executionHandler);
        pipeline.addLast("idleStateHandler", new IdleStateHandler(this.timer, 0, 0, serverTimeoutMillis,
                TimeUnit.MILLISECONDS));
        pipeline.addLast("traceMarkerhandler", traceMarkerhandler);
        pipeline.addLast("servletHandler", servletHandler);
        pipeline.addLast("handler", httpRequestHandlerProvider.get());
        return pipeline;
    }
}
