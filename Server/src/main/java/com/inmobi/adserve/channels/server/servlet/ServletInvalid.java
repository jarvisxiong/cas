package com.inmobi.adserve.channels.server.servlet;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


@Singleton
public class ServletInvalid implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletInvalid.class);

    @SuppressWarnings("deprecation")
    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        // invalid request
        // TODO: remove header validation
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer("Page not Found", Charset.defaultCharset()), true);

        if (serverChannel != null && serverChannel.isWritable()) {
            ChannelFuture future = serverChannel.writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public String getName() {
        return "Invalid Servlet";
    }

}
