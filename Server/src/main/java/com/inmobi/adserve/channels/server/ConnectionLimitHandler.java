package com.inmobi.adserve.channels.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;


@Singleton
@Sharable
public class ConnectionLimitHandler extends ChannelDuplexHandler {

    private final static Logger LOG                = LoggerFactory.getLogger(ConnectionLimitHandler.class);

    @Getter
    private final AtomicInteger activeConnections  = new AtomicInteger(0);
    @Getter
    private final AtomicInteger droppedConnections = new AtomicInteger(0);

    private final ServerConfig  serverConfig;

	private int maxConnections;

    @Inject
    public ConnectionLimitHandler(final ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        maxConnections = getMaxConnectionsLimit() * Runtime.getRuntime().availableProcessors();
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        if (maxConnections > 0 && activeConnections.getAndIncrement() > maxConnections) {
            ctx.channel().close();
            LOG.error("Incoming MaxLimit of connections {} exceeded so closing channel", maxConnections);
            droppedConnections.incrementAndGet();

        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0 && activeConnections.decrementAndGet() < 0) {
            LOG.error("BUG in ConnectionLimitHandler");
        }
        super.channelUnregistered(ctx);
    }

    public int getMaxConnectionsLimit() {
        return serverConfig.getMaxIncomingConnections();
    }

}
