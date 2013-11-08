package com.inmobi.adserve.channels.server;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.concurrent.TimeUnit;

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


public class ChannelServerPipelineFactory implements ChannelPipelineFactory
{

    private final Timer      timer;
    private int              serverTimeoutMillis; ;
    private ExecutionHandler executionHandler;

    public ChannelServerPipelineFactory(Timer timer, Configuration configuration)
    {
        this.timer = timer;
        try {
            this.serverTimeoutMillis = configuration.getInt("serverTimeoutMillis");
        }
        catch (Exception e) {
            this.serverTimeoutMillis = 825;
        }
        executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(80, 1048576, 1048576, 3,
                TimeUnit.HOURS));
    }

    public ChannelPipeline getPipeline() throws Exception
    {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("httpchunkhandler", new HttpChunkAggregator(100000000));
        pipeline.addLast("executionHandler", executionHandler);
        pipeline.addLast("idleStateHandler", new IdleStateHandler(this.timer, 0, 0, serverTimeoutMillis,
                TimeUnit.MILLISECONDS));
        pipeline.addLast("handler", new HttpRequestHandler());
        return pipeline;
    }
}
