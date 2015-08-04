package com.inmobi.adserve.channels.server;

import org.slf4j.MDC;

import com.google.inject.Singleton;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * @author abhishek.parwal
 *
 */
@Sharable
@Singleton
public class RequestIdHandler extends ChannelInboundHandlerAdapter {
    public RequestIdHandler() {}

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        MDC.put("requestId", String.format("0x%08x", ctx.channel().hashCode()));
        ctx.fireChannelRead(msg);
    }
}
