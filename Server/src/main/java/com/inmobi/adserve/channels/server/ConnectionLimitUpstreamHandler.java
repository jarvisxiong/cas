package com.inmobi.adserve.channels.server;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import com.inmobi.adserve.channels.util.DebugLogger;

public class ConnectionLimitUpstreamHandler extends SimpleChannelHandler {

  private final AtomicInteger connections = new AtomicInteger(0);
  private int maxConnections;
  private int droppedConnections;
  private DebugLogger logger;

  public ConnectionLimitUpstreamHandler(int maxConnections) {
    this.maxConnections = maxConnections;
    this.droppedConnections = 0;
    this.logger = new DebugLogger();
  }

  @Override
  public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    if(maxConnections > 0) {
      int currentCount = connections.getAndIncrement();
      if(currentCount + 1 > maxConnections) {
        if(logger.isDebugEnabled())
          logger.error("MaxLimit of connections " + maxConnections + " exceeded so closing channel");
        ctx.getChannel().close();
        droppedConnections++;
      }
    }

    super.channelOpen(ctx, e);
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    if(maxConnections > 0) {
      connections.decrementAndGet();
    }

    super.channelClosed(ctx, e);
  }

  public int getActiveOutboundConnections() {
    return connections.get();
  }

  public void setMaxConnections(int maxConnection) {
    if(maxConnection > 0)
      maxConnections = maxConnection;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public int getDroppedConnections() {
    return droppedConnections;
  }

}