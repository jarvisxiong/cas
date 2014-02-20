package com.inmobi.adserve.channels.server;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.MDC;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
public class RequestIdHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        MDC.put("requestId", e.getChannel().getId().toString());
        super.messageReceived(ctx, e);
    }

}
