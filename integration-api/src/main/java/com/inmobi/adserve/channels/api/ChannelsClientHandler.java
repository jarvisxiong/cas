package com.inmobi.adserve.channels.api;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ConcurrentHashMap;


public class ChannelsClientHandler extends ChannelDuplexHandler {
    public static final ConcurrentHashMap<Integer, StringBuffer>       responseMap = new ConcurrentHashMap<Integer, StringBuffer>();
    public static final ConcurrentHashMap<Integer, HttpResponseStatus> statusMap   = new ConcurrentHashMap<Integer, HttpResponseStatus>(
                                                                                           128);
    public static final ConcurrentHashMap<Integer, String>             adStatusMap = new ConcurrentHashMap<Integer, String>(
                                                                                           128);

    public static StringBuffer getMessage(final int channelId) {
        return responseMap.get(channelId);
    }

    public static void removeEntry(final int channelId) {
        responseMap.remove(channelId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext,
     * java.lang.Object)
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        int channelId = ctx.channel().hashCode();

        StringBuffer msgContent = new StringBuffer(((FullHttpResponse) msg).content().toString(CharsetUtil.UTF_8));
        responseMap.put(channelId, msgContent);
        HttpResponseStatus status = ((HttpResponse) msg).getStatus();
        statusMap.put(channelId, status);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            adStatusMap.put(ctx.channel().hashCode(), "TIME_OUT");
        }
        ctx.channel().close();
    }

}
