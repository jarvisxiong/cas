package com.inmobi.adserve.channels.api;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.util.EstimatableObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Slf4j
public class CustomChannelEventRunnable implements Runnable, EstimatableObjectWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(CustomChannelEventRunnable.class);

    private final ChannelHandlerContext ctx;
    private final ChannelEvent e;
    int estimatedSize;

    /**
     * Creates a {@link Runnable} which sends the specified {@link ChannelEvent}
     * upstream via the specified {@link ChannelHandlerContext}.
     */
    public CustomChannelEventRunnable(ChannelHandlerContext ctx, ChannelEvent e) {
        this.ctx = ctx;
        this.e = e;
    }

    /**
     * Returns the {@link ChannelHandlerContext} which will be used to
     * send the {@link ChannelEvent} upstream.
     */
    public ChannelHandlerContext getContext() {
        return ctx;
    }

    /**
     * Returns the {@link ChannelEvent} which will be sent upstream.
     */
    public ChannelEvent getEvent() {
        return e;
    }

    /**
     * Sends the event upstream.
     */
    public void run() {
        LOG.debug("Generating new requestId 2 {}", ctx.getChannel().getId());
        MDC.put("requestId", ctx.getChannel().getId().toString());
        ctx.sendUpstream(e);
    }

    public Object unwrap() {
        return e;
    }
}
