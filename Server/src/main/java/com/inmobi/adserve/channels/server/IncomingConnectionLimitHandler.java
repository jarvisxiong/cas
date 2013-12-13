package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.util.DebugLogger;
import lombok.Getter;
import lombok.Setter;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class IncomingConnectionLimitHandler extends SimpleChannelHandler {
    @Getter
    private static final AtomicInteger incomingActiveConnections = new AtomicInteger(0);
    @Getter @Setter
    private static int                 incomingMaxConnections;
    @Getter
    private static int                 incomingDroppedConnections;
    private DebugLogger logger;

    public IncomingConnectionLimitHandler(int maxConnections) {
        incomingMaxConnections = maxConnections;
        incomingDroppedConnections = 0;
        this.logger = new DebugLogger();
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (incomingMaxConnections > 0) {
            int currentCount = incomingActiveConnections.getAndIncrement();
            if (currentCount + 1 > incomingMaxConnections) {
                logger.info("Incoming MaxLimit of connections", incomingMaxConnections, "exceeded so closing channel");
                ctx.getChannel().close();
                incomingDroppedConnections++;
            }
        }
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (incomingMaxConnections > 0) {
            incomingActiveConnections.decrementAndGet();
        }

        super.channelClosed(ctx, e);
    }
}
