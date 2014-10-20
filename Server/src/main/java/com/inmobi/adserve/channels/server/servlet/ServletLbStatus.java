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

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
@Path("/lbstatus")
public class ServletLbStatus implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletLbStatus.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        LOG.debug("asked for load balancer status");
        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.TOTAL_REQUESTS);
        if (ServerStatusInfo.statusCode != 404) {
            InspectorStats.incrementStatCount("LbStatus", InspectorStrings.SUCCESSFUL_REQUESTS);
            hrh.responseSender.sendResponse("OK", serverChannel);
            return;
        }
        // TODO: remove header validation
        final HttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer(
                        ServerStatusInfo.statusString, Charset.defaultCharset()));
        final ChannelFuture future = serverChannel.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public String getName() {
        return "lbstatus";
    }
}
