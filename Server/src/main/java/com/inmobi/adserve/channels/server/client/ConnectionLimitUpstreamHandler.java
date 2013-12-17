package com.inmobi.adserve.channels.server.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectionLimitUpstreamHandler extends SimpleChannelHandler {
    private static final Logger LOG         = LoggerFactory.getLogger(ConnectionLimitUpstreamHandler.class);

    private final AtomicInteger connections = new AtomicInteger(0);
    private int                 maxConnections;
    private int                 droppedConnections;

    public ConnectionLimitUpstreamHandler(final int maxConnections) {
        this.maxConnections = maxConnections;
        this.droppedConnections = 0;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        if (maxConnections > 0) {
            int currentCount = connections.getAndIncrement();
            if (currentCount + 1 > maxConnections) {
                LOG.info("MaxLimit of connections {} exceeded so closing channel", maxConnections);
                ctx.getChannel().close();
                droppedConnections++;
            }
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        if (maxConnections > 0) {
            connections.decrementAndGet();
        }

        super.channelClosed(ctx, e);
    }

    public int getActiveOutboundConnections() {
        return connections.get();
    }

    public void setMaxConnections(final int maxConnection) {
        if (maxConnection > 0) {
            maxConnections = maxConnection;
        }
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getDroppedConnections() {
        return droppedConnections;
    }

}