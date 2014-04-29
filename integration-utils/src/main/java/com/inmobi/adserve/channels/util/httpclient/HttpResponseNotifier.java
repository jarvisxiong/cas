package com.inmobi.adserve.channels.util.httpclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.concurrent.Promise;

import com.inmobi.adserve.channels.util.httpclient.ConnectionPoolImpl.PoolKey;


/**
 * @author abhishek.parwal
 * 
 */
public class HttpResponseNotifier extends ChannelInboundHandlerAdapter {

    private Promise<DefaultFullHttpResponse> httpResponsePromise;
    private final ConnectionPool             connectionPool;
    private final PoolKey                    poolKey;

    public HttpResponseNotifier(final ConnectionPool connectionPool, final PoolKey poolKey) {
        this.connectionPool = connectionPool;
        this.poolKey = poolKey;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        DefaultFullHttpResponse httpResponse = (DefaultFullHttpResponse) msg;
        HttpHeaders.setKeepAlive(httpResponse, true);
        httpResponsePromise.setSuccess(httpResponse);
        connectionPool.freeChannel(ctx.channel(), poolKey);
        super.channelRead(ctx, msg);
    }

    public void setResponsePromise(final Promise<DefaultFullHttpResponse> httpResponsePromise) {
        this.httpResponsePromise = httpResponsePromise;
    }
}