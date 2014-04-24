package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.thrift.TException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class HttpRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestHandler.class);

    public String               terminationReason;
    public ResponseSender       responseSender;

    private Marker              traceMarker;
    private Servlet             servlet;

    private HttpRequest         httpRequest;

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public HttpRequestHandler() {
        responseSender = new ResponseSender(this);
    }

    @Inject
    HttpRequestHandler(final Marker traceMarker, final Servlet servlet) {
        this.traceMarker = traceMarker;
        this.servlet = servlet;
        responseSender = new ResponseSender(this);
    }

    /**
     * Invoked when an exception occurs whenever channel throws closedchannelexception increment the totalterminate
     * means channel is closed by party who requested for the ad
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        MDC.put("requestId", String.format("0x%08x", ctx.channel().hashCode()));

        if (cause instanceof ReadTimeoutException) {

            if (ctx.channel().isOpen()) {
                LOG.debug(traceMarker, "Channel is open in channelIdle handler");
                if (responseSender.getRankList() != null) {
                    for (ChannelSegment channelSegment : responseSender.getRankList()) {
                        if (channelSegment.getAdNetworkInterface().getAdStatus().equals("AD")) {
                            LOG.debug(traceMarker, "Got Ad from {} Top Rank was {}", channelSegment
                                    .getAdNetworkInterface().getName(), responseSender.getRankList().get(0)
                                    .getAdNetworkInterface().getName());
                            responseSender.sendAdResponse(channelSegment.getAdNetworkInterface(), ctx.channel());
                            return;
                        }
                    }
                }
                responseSender.sendNoAdResponse(ctx.channel());
            }
            // increment the totalTimeout. It means server
            // could not write the response with in 800 ms
            LOG.debug(traceMarker, "inside channel idle event handler for Request channel ID: {}", ctx.channel());
            InspectorStats.incrementStatCount(InspectorStrings.totalTimeout);
            LOG.debug(traceMarker, "server timeout");

        }
        else {

            String exceptionString = cause.getClass().getSimpleName();
            InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
            InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
            if (cause instanceof ClosedChannelException || cause instanceof IOException) {
                InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
                LOG.debug(traceMarker, "Channel is terminated {}", ctx.channel());
            }
            LOG.info(traceMarker, "Getting netty error in HttpRequestHandler: {}", cause);
            if (ctx.channel().isOpen()) {
                responseSender.sendNoAdResponse(ctx.channel());
            }
        }

    }

    // Invoked when message is received over the connection
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        RequestParameterHolder requestParameterHolder = (RequestParameterHolder) msg;
        try {
            this.terminationReason = requestParameterHolder.getTerminationReason();
            this.responseSender.sasParams = requestParameterHolder.getSasParams();
            this.responseSender.casInternalRequestParameters = requestParameterHolder.getCasInternalRequestParameters();
            httpRequest = requestParameterHolder.getHttpRequest();

            LOG.debug(traceMarker, "Got the servlet {} , uri {}", servlet.getName(), httpRequest.getUri());

            servlet.handleRequest(this, new QueryStringDecoder(httpRequest.getUri()), ctx.channel());
            return;
        }
        catch (Exception exception) {
            terminationReason = ServletHandler.processingError;
            InspectorStats.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
            responseSender.sendNoAdResponse(ctx.channel());
            String exceptionClass = exception.getClass().getSimpleName();
            InspectorStats.incrementStatCount(exceptionClass, InspectorStrings.count);
            LOG.info(traceMarker, "stack trace is {}", exception);
        }
        finally {
            requestParameterHolder.getHttpRequest().release();
        }

    }

    public boolean isRequestFromLocalHost() {
        String host = CasUtils.getHost(httpRequest);

        if (host != null && host.startsWith("localhost")) {
            return true;
        }

        return false;
    }

    /**
     * @return the httpRequest
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void writeLogs(final ResponseSender responseSender) {
        List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        if (null != responseSender.getRankList()) {
            list.addAll(responseSender.getRankList());
        }
        if (null != responseSender.getAuctionEngine().getRtbSegments()) {
            list.addAll(responseSender.getAuctionEngine().getRtbSegments());
        }
        long totalTime = responseSender.getTotalTime();
        if (totalTime > 2000) {
            totalTime = 0;
        }
        try {
            ChannelSegment adResponseChannelSegment = null;
            if (null != responseSender.getRtbResponse()) {
                adResponseChannelSegment = responseSender.getRtbResponse();
            }
            else if (null != responseSender.getAdResponse()) {
                adResponseChannelSegment = responseSender.getRankList().get(responseSender.getSelectedAdIndex());
            }
            Logging.rrLogging(adResponseChannelSegment, list, responseSender.sasParams, terminationReason, totalTime);
            Logging.advertiserLogging(list, ServletHandler.getLoggerConfig());
            Logging.sampledAdvertiserLogging(list, ServletHandler.getLoggerConfig());
        }
        catch (JSONException exception) {
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        }
        catch (TException exception) {
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        }
        LOG.debug("done with logging");
    }

}
