package com.inmobi.adserve.channels.util.httpclient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.inmobi.adserve.channels.util.httpclient.ConnectionPoolImpl.PoolKey;
import com.inmobi.adserve.channels.util.httpclient.NettyRequest.NettyRequestType;


/**
 * @author abhishek.parwal
 * 
 */
@Slf4j
public class NettyHttpClientImpl implements NettyHttpClient {

    private final ConnectionPool connectionPool;

    // TODO: requestTimeoutInMillis , idleConnectionInMillis
    public NettyHttpClientImpl(final int connectTimeoutInMillis, final int requestTimeoutInMillis,
            final int maximumConnectionsTotal) {
        connectionPool = new ConnectionPoolImpl(connectTimeoutInMillis, requestTimeoutInMillis, maximumConnectionsTotal);
    }

    @Override
    public Future<DefaultFullHttpResponse> sendRequest(final NettyRequest nettyRequest, final EventLoop eventLoop) {
        final Promise<DefaultFullHttpResponse> httpResponsePromise = eventLoop.newPromise();
        try {
            Channel channel = connectAndSendRequest(nettyRequest, eventLoop, new ChannelWriteListener(nettyRequest,
                    httpResponsePromise));
            log.debug("Got a connection : {}", channel);
        }
        catch (Exception exception) {
            log.error("Coudn't get a connection: {}", exception);
        }

        return httpResponsePromise;
    }

    private Channel connectAndSendRequest(final NettyRequest nettyRequest, final EventLoop eventLoop,
            final ChannelWriteListener channelWriteListener) throws Exception {

        Channel channel = connectionPool.getChannel(nettyRequest, eventLoop, channelWriteListener);
        return channel;
    }

    private static HttpRequest getHttpRequest(final NettyRequest nettyRequest) {

        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                nettyRequest.getNettyRequestType() == NettyRequestType.GET ? HttpMethod.GET : HttpMethod.POST,
                nettyRequest.getUri().getRawPath());
        httpRequest.headers().set(HttpHeaders.Names.HOST, nettyRequest.getUri().getHost());
        HttpHeaders.setKeepAlive(httpRequest, true);

        Map<String, String> headerMap = nettyRequest.getHeaderMap();

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            httpRequest.headers().set(entry.getKey(), entry.getValue());

        }

        return httpRequest;
    }

    public static class HttpResponseNotifier extends ChannelInboundHandlerAdapter {

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

    public static class ChannelWriteListener implements ChannelFutureListener {

        private final NettyRequest                     nettyRequest;
        private final Promise<DefaultFullHttpResponse> httpResponsePromise;

        public ChannelWriteListener(final NettyRequest nettyRequest,
                final Promise<DefaultFullHttpResponse> httpResponsePromise) {
            this.nettyRequest = nettyRequest;
            this.httpResponsePromise = httpResponsePromise;
        }

        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                future.channel().writeAndFlush(getHttpRequest(nettyRequest)).addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(final ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().pipeline().get(HttpResponseNotifier.class)
                                    .setResponsePromise(httpResponsePromise);
                            future.channel().read();
                        }
                        else {
                            httpResponsePromise.setFailure(future.cause());
                            future.channel().close();
                        }
                    }
                });
            }
            else {
                httpResponsePromise.setFailure(future.cause());
                future.channel().close();
            }

        }
    }

    public static void main(final String[] args) {

        EventLoop nioEventLoop = new NioEventLoopGroup().next();
        for (int i = 0; i < 10; i++) {

            new NettyHttpClientImpl(1000, 1000, 1000).sendRequest(
                    new NettyRequest().setUri(URI.create("http://gooogle.com:80/")), nioEventLoop).addListener(
                    new GenericFutureListener<Future<FullHttpResponse>>() {

                        @Override
                        public void operationComplete(final Future<FullHttpResponse> future) throws Exception {
                            FullHttpResponse httpResponse = future.get();
                            System.out.println(httpResponse.getStatus() + " " + httpResponse.content());
                        }

                    });
        }
    }
}
