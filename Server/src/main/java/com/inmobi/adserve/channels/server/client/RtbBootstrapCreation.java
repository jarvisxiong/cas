package com.inmobi.adserve.channels.server.client;

import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.Timer;

import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.server.RequestIdHandler;
import com.inmobi.adserve.channels.server.TraceMarkerhandler;


public class RtbBootstrapCreation {
    public static ClientBootstrap                 bootstrap;
    private static ChannelsClientHandler          channelHandler;
    private static Timer                          timer;
    private static ConnectionLimitUpstreamHandler connectionLimitUpstreamHandler;

    public static void init(final Timer timer) {
        RtbBootstrapCreation.timer = timer;
    }

    public static ClientBootstrap createBootstrap(final Logger logger, final Configuration config) {
        // make the bootstrap object
        try {
            bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()));
            channelHandler = new ChannelsClientHandler();
            int maxConnections;
            try {
                maxConnections = config.getInt("maxconnections");
            }
            catch (NoSuchElementException e) {
                maxConnections = 100;
            }
            connectionLimitUpstreamHandler = new ConnectionLimitUpstreamHandler(maxConnections);
        }
        catch (Exception ex) {
            logger.info("error in building RTBbootstrap " + ex.getMessage());
            return null;
        }

        final RequestIdHandler requestIdHandler = new RequestIdHandler();
        final TraceMarkerhandler traceMarkerhandler = new TraceMarkerhandler();

        try {
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                @Override
                public ChannelPipeline getPipeline() throws Exception {
                    int rtbReadTimeoutMills;
                    try {
                        rtbReadTimeoutMills = config.getInt("RTBreadtimeoutMillis");
                    }
                    catch (NoSuchElementException e) {
                        rtbReadTimeoutMills = 200;
                    }
                    ChannelPipeline pipeline = Channels.pipeline();
                    pipeline.addLast("requestIdHandler", requestIdHandler);
                    pipeline.addLast("traceMarkerhandler", traceMarkerhandler);
                    pipeline.addLast("connectionLimit", connectionLimitUpstreamHandler);
                    pipeline.addLast("timeout", new ReadTimeoutHandler(timer, rtbReadTimeoutMills,
                            TimeUnit.MILLISECONDS));
                    pipeline.addLast("encoder", new HttpRequestEncoder());
                    pipeline.addLast("decoder", new HttpResponseDecoder());
                    pipeline.addLast("handler", channelHandler);
                    return pipeline;
                }
            });
        }
        catch (Exception ex) {
            logger.info("error in creating pipeline " + ex.getMessage());
            return null;
        }
        // set bootstrap options
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("reuseAddress", true);
        return bootstrap;
    }

    public static void setMaxConnectionLimit(final int maxOutboundConnectionLimit) {
        connectionLimitUpstreamHandler.setMaxConnections(maxOutboundConnectionLimit);
    }

    public static int getActiveOutboundConnections() {
        return connectionLimitUpstreamHandler.getActiveOutboundConnections();
    }

    public static int getMaxConnections() {
        return connectionLimitUpstreamHandler.getMaxConnections();
    }

    public static int getDroppedConnections() {
        return connectionLimitUpstreamHandler.getDroppedConnections();
    }

}
