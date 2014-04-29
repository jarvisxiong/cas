package com.inmobi.adserve.channels.util.httpclient;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Promise;

import java.util.Map;

import com.inmobi.adserve.channels.util.httpclient.NettyRequest.NettyRequestType;


/**
 * @author abhishek.parwal
 * 
 */
public class ChannelWriteListener implements ChannelFutureListener {

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

    private HttpRequest getHttpRequest(final NettyRequest nettyRequest) {

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
}