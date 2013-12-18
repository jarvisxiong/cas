package com.inmobi.adserve.channels.api;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;


public class ChannelsClientHandler extends SimpleChannelHandler {
    public static final ConcurrentHashMap<Integer, StringBuffer>       responseMap            = new ConcurrentHashMap<Integer, StringBuffer>();
    public static final ConcurrentHashMap<Integer, HttpResponseStatus> statusMap              = new ConcurrentHashMap<Integer, HttpResponseStatus>(
                                                                                                      128);
    public static final ConcurrentHashMap<Integer, String>             adStatusMap            = new ConcurrentHashMap<Integer, String>(
                                                                                                      128);
    private static final String                                        READ_TIMEOUT_EXCEPTION = "org.jboss.netty.handler.timeout.ReadTimeoutException";

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    }

    public static StringBuffer getMessage(int channelId) {
        return responseMap.get(channelId);
    }

    public static void removeEntry(int channelId) {
        responseMap.remove(channelId);
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        int channelId = ctx.getChannel().getId();

        if (e.getMessage() instanceof HttpResponse) {
            StringBuffer msgContent = new StringBuffer(((HttpResponse) e.getMessage()).getContent().toString(
                CharsetUtil.UTF_8));
            responseMap.put(channelId, msgContent);
            HttpResponseStatus status = ((HttpResponse) e.getMessage()).getStatus();
            statusMap.put(channelId, status);
        }
        /*
         * //Http Message is chunked else if(e.getMessage() instanceof HttpMessage ){ HttpMessage content =
         * (HttpMessage)e.getMessage(); //Here HttpMessage has come, to be followed by HttpChunks later....
         * HttpResponseStatus status = ((HttpResponse)content).getStatus(); statusMap.put(channelId, status); }
         */
        else {
            // HttpChunk has arrived....
            if (responseMap.get(channelId) != null) {
                responseMap
                        .get(channelId)
                            .append(((HttpChunk) e.getMessage()).getContent().toString(CharsetUtil.UTF_8));
            }
            else {
                StringBuffer msgContent = new StringBuffer(((HttpChunk) e.getMessage()).getContent().toString(
                    CharsetUtil.UTF_8));
                responseMap.put(channelId, msgContent);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        String cause = e.getCause().toString();
        if (cause.equals(READ_TIMEOUT_EXCEPTION)) {
            adStatusMap.put(ctx.getChannel().getId(), "TIME_OUT");
        }
        e.getChannel().close();

    }
}
