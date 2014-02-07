package com.inmobi.adserve.channels.server;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Random;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
@Slf4j
public class RequestIdHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOG               = LoggerFactory.getLogger(RequestIdHandler.class);

    private final Random randomNumberGenerator;

    public RequestIdHandler() {
        randomNumberGenerator = new Random();
    }

    
    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String requestId = String.format("%s-%s", System.currentTimeMillis(), randomNumberGenerator.nextInt(99999999));
        LOG.error("Generating New requestId.... {} ", requestId);
        MDC.put("requestId", requestId);
        super.messageReceived(ctx,e);
    }
     

}
