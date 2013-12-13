package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.util.DebugLogger;
import lombok.Getter;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionLimitHandler extends SimpleChannelHandler {
    @Getter
    private AtomicInteger activeConnections = new AtomicInteger(0);
    @Getter
    private AtomicInteger droppedConnections = new AtomicInteger(0);
    private DebugLogger logger;
    private ConnectionType connectionType;
    private Configuration config;

    public ConnectionLimitHandler(Configuration configuration, ConnectionType connType) {
        config = configuration;
        connectionType = connType;
        this.logger = new DebugLogger();
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0) {
            int currentCount = activeConnections.getAndIncrement();
            if (currentCount + 1 > maxConnections) {
                logger.info(connectionType.name(), "MaxLimit of connections", maxConnections, "exceeded so closing channel");
                ctx.getChannel().close();
                droppedConnections.incrementAndGet();
            }
        }
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        int maxConnections = getMaxConnectionsLimit();
        if (maxConnections > 0) {
            activeConnections.decrementAndGet();
        }

        super.channelClosed(ctx, e);
    }
    
    public int getMaxConnectionsLimit() {
        int maxConnections = 200;
      switch (connectionType) {
          case DCPOutGoing :
              maxConnections = config.getInt("dcpOutGoingMaxConnections", 200);
              break;
          case RTBDOutGoing:
              maxConnections = config.getInt("rtbOutGoingMaxConnections", 200);
              break;
          case Incoming:
              maxConnections = config.getInt("incomingMaxConnections", 500);
              break;
          default : break;
      }
        return maxConnections;
    }
}
