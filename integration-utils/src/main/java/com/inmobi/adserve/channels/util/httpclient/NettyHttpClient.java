package com.inmobi.adserve.channels.util.httpclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;


/**
 * @author abhishek.parwal
 * 
 */
public class NettyHttpClient {

    private void setRequestTimeoutInMs() {
        Bootstrap bootstrap = null;
        ChannelHandlerContext ctx = null;
        bootstrap.group(ctx.channel().eventLoop());
        // TODO Auto-generated method stub

    }
}
