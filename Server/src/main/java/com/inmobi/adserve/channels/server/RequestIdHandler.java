package com.inmobi.adserve.channels.server;

import java.util.Random;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.MDC;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
public class RequestIdHandler extends SimpleChannelUpstreamHandler {

    private final Random randomNumberGenerator;

    public RequestIdHandler() {
        randomNumberGenerator = new Random();
    }

    @Override
    public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {

        MDC.put("requestId",
                String.format("%s-%s", System.currentTimeMillis(), randomNumberGenerator.nextInt(99999999)));
        super.handleUpstream(ctx, e);
    }
}
