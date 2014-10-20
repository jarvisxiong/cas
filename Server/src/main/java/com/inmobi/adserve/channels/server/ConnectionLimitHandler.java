package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.config.ServerConfig;


@Singleton
@Sharable
@Slf4j
public class ConnectionLimitHandler extends ChannelDuplexHandler {
    @Getter
    private final AtomicInteger activeConnections = new AtomicInteger(0);
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
            log.error("Incoming MaxLimit of connections {} exceeded so closing channel", maxConnections);
            activeConnections.decrementAndGet();
            droppedConnections.incrementAndGet();

        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (activeConnections.decrementAndGet() < 0) {
            activeConnections.incrementAndGet();
            log.error("BUG in ConnectionLimitHandler");
        }
        super.channelUnregistered(ctx);
    }

    /**
     * 
     * @return
     */
    public JSONObject getConnectionJson() {
        final JSONObject connection = new JSONObject();
        try {
            connection.put("IncomingMaxConnections", getMaxConnections());
            connection.put("IncomingDroppedConnections", getDroppedConnections());
            connection.put("IncomingActiveConnections", getActiveConnections());
            connection.put("AvailableProcessors", Runtime.getRuntime().availableProcessors());
        } catch (final JSONException exp) {
            log.error("Error in getting getConnectionJson", exp);
        }
        return connection;
    }
}
