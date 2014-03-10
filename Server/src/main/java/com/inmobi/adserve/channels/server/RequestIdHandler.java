package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.MDC;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
public class RequestIdHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        MDC.put("requestId", String.format("0x%08x", ctx.channel().hashCode()));
        ctx.fireChannelRead(msg);
    }

}
