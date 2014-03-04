package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.MDC;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
public class RequestIdHandler extends SimpleChannelInboundHandler<HttpRequest> {

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpRequest httpRequest) {
        MDC.put("requestId", String.valueOf(ctx.channel().hashCode()));
        ctx.fireChannelRead(httpRequest);
    }

}
