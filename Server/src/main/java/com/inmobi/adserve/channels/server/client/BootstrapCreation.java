package com.inmobi.adserve.channels.server.client;

import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.api.ConnectionType;
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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class BootstrapCreation {
    public static ClientBootstrap                 bootstrap;
    private static ChannelsClientHandler          channelHandler;
    private static Timer                          timer;
    private static ConnectionLimitHandler connectionLimitUpstreamHandler;

    public static void init(Timer timer) {
        BootstrapCreation.timer = timer;
    }

    public static ClientBootstrap createBootstrap(Logger logger, final Configuration config) {
        // make the bootstrap object
        try {
            bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()));
            channelHandler = new ChannelsClientHandler();
            connectionLimitUpstreamHandler = new ConnectionLimitHandler(config, ConnectionType.DCPOutGoing);
        }
        catch (Exception ex) {
            logger.info("error in building bootstrap " + ex.getMessage());
            return null;
        }
        // make the channel pipeline
        try {
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline pipeline = Channels.pipeline();
                    pipeline.addLast("connectionLimit", connectionLimitUpstreamHandler);
                    pipeline.addLast("timeout", new ReadTimeoutHandler(timer, config.getInt("readtimeoutMillis"),
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

    public static int getActiveOutboundConnections() {
        return connectionLimitUpstreamHandler.getActiveConnections().get();
    }

    public static int getMaxConnections() {
        return connectionLimitUpstreamHandler.getMaxConnectionsLimit();
    }

    public static int getDroppedConnections() {
        return connectionLimitUpstreamHandler.getDroppedConnections().get();
    }

}
