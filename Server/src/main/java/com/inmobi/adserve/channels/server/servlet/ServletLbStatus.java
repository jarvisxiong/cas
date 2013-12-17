package com.inmobi.adserve.channels.server.servlet;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class ServletLbStatus implements Servlet {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ServletLbStatus.class);

    @SuppressWarnings("deprecation")
    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        // Initializing loggers for expected rotation format
        if (++ServletHandler.rollCount == 20) {
            Logger rrLogger = Logger.getLogger(ServletHandler.getLoggerConfig().getString("rr"));
            if (null != rrLogger) {
                rrLogger.debug("");
            }
            Logger advertiserLogger = Logger.getLogger(ServletHandler.getLoggerConfig().getString("advertiser"));
            if (null != advertiserLogger) {
                advertiserLogger.debug("");
            }
            Logger channelLogger = Logger.getLogger(ServletHandler.getLoggerConfig().getString("channel"));
            if (null != channelLogger) {
                channelLogger.debug("");
            }
            ServletHandler.rollCount = 0;
        }
        LOG.debug("asked for load balancer status");
        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.totalRequests);
        if (ServerStatusInfo.statusCode != 404) {
            InspectorStats.incrementStatCount("LbStatus", InspectorStrings.successfulRequests);
            hrh.responseSender.sendResponse("OK", e);
            return;
        }
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
        response
                .setContent(ChannelBuffers.copiedBuffer(ServerStatusInfo.statusString, Charset.forName("UTF-8").name()));
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
        return "lbstatus";
    }
}
