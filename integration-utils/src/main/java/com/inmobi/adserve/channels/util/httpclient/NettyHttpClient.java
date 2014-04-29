package com.inmobi.adserve.channels.util.httpclient;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.util.concurrent.Future;


/**
 * @author abhishek.parwal
 * 
 */
public interface NettyHttpClient {

    Future<DefaultFullHttpResponse> sendRequest(final NettyRequest nettyRequest, final EventLoop eventLoop);
}
