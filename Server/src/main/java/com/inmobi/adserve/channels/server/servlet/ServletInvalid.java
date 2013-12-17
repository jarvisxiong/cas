package com.inmobi.adserve.channels.server.servlet;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


public class ServletInvalid implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletInvalid.class);

    @SuppressWarnings("deprecation")
    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        // invalid request
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
        response.setContent(ChannelBuffers.copiedBuffer("Page not Found", Charset.forName("UTF-8").name()));
        if (e != null) {
            Channel channel = e.getChannel();
            if (channel != null && channel.isWritable()) {
                ChannelFuture future = channel.write(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public String getName() {
        return "Invalid Servlet";
    }

}
