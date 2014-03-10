package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.config.ServerConfig;


@Sharable
public class ConnectionLimitHandler extends ChannelDuplexHandler {
    private final static Logger  LOG                = LoggerFactory.getLogger(ConnectionLimitHandler.class);

    @Getter
    private final AtomicInteger  activeConnections  = new AtomicInteger(0);
    @Getter
    private final AtomicInteger  droppedConnections = new AtomicInteger(0);
    private final ConnectionType connectionType;

    private final ServerConfig   serverConfig;

    public ConnectionLimitHandler(final ServerConfig serverConfig, final ConnectionType connType) {
        this.serverConfig = serverConfig;
        connectionType = connType;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0) {
            int currentCount = activeConnections.getAndIncrement();
            if (currentCount > maxConnections) {
                LOG.info("{} MaxLimit of connections {} exceeded so closing channel", connectionType.name(),
                        maxConnections);
                ctx.channel().close();
                droppedConnections.incrementAndGet();
            }
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0) {
            activeConnections.decrementAndGet();
        }
        super.channelInactive(ctx);
    }

    public int getMaxConnectionsLimit() {
        switch (connectionType) {
            case DCP_OUTGOING:
                return serverConfig.getMaxDcpOutGoingConnections();
            case RTBD_OUTGOING:
                return serverConfig.getMaxRtbOutGoingConnections();
            case INCOMING:
                return serverConfig.getMaxIncomingConnections();
            default:
                throw new RuntimeException();
        }
    }

}
