package com.inmobi.adserve.channels.server;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.api.ConnectionType;


public class ConnectionLimitHandler extends SimpleChannelHandler {
    private final static Logger  LOG                = LoggerFactory.getLogger(ConnectionLimitHandler.class);

    @Getter
    private final AtomicInteger  activeConnections  = new AtomicInteger(0);
    @Getter
    private final AtomicInteger  droppedConnections = new AtomicInteger(0);
    private final ConnectionType connectionType;
    private final Configuration  config;

    public ConnectionLimitHandler(final Configuration configuration, final ConnectionType connType) {
        config = configuration;
        connectionType = connType;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0) {
            int currentCount = activeConnections.getAndIncrement();
            if (currentCount > maxConnections) {
                LOG.info("{} MaxLimit of connections {} exceeded so closing channel", connectionType.name(),
                    maxConnections);
                ctx.getChannel().close();
                droppedConnections.incrementAndGet();
            }
        }
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0) {
            activeConnections.decrementAndGet();
        }

        super.channelClosed(ctx, e);
    }

    public int getMaxConnectionsLimit() {
        int maxConnections = 200;
        switch (connectionType) {
            case DCP_OUTGOING:
                maxConnections = config.getInt("dcpOutGoingMaxConnections", 200);
                break;
            case RTBD_OUTGOING:
                maxConnections = config.getInt("rtbOutGoingMaxConnections", 200);
                break;
            case INCOMING:
                maxConnections = config.getInt("incomingMaxConnections", 500);
                break;
            default:
                break;
        }
        return maxConnections;
    }
}
