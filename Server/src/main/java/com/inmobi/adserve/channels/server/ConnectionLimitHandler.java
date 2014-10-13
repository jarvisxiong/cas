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
    @Getter
	private final int maxConnections;

    @Inject
    public ConnectionLimitHandler(final ServerConfig serverConfig) throws Exception {
        maxConnections = serverConfig.getMaxIncomingConnections() * Runtime.getRuntime().availableProcessors();
        if (maxConnections <= 0) {
            throw new Exception("Max connection can not be less or equal to zero");
        }
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        if (activeConnections.incrementAndGet() > maxConnections) {
            ctx.channel().close();
            LOG.error("Incoming MaxLimit of connections {} exceeded so closing channel", maxConnections);
            activeConnections.decrementAndGet();
            droppedConnections.incrementAndGet();

        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (activeConnections.decrementAndGet() < 0) {
            activeConnections.incrementAndGet();
            LOG.error("BUG in ConnectionLimitHandler");
        }
        super.channelUnregistered(ctx);
    }
}
