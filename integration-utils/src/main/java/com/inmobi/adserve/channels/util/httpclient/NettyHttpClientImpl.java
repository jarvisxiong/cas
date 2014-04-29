package com.inmobi.adserve.channels.util.httpclient;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;


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
            connectAndSendRequest(nettyRequest, eventLoop, new ChannelWriteListener(nettyRequest, httpResponsePromise));
        }
        catch (Exception exception) {
            log.error("Coudn't get a connection: {}", exception);
        }

        return httpResponsePromise;
    }

    private void connectAndSendRequest(final NettyRequest nettyRequest, final EventLoop eventLoop,
            final ChannelWriteListener channelWriteListener) throws Exception {

        connectionPool.getChannel(nettyRequest, eventLoop, channelWriteListener);
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
