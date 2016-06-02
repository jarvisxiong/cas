package com.inmobi.adserve.channels.server;

import static com.inmobi.adserve.channels.util.InspectorStrings.COUNT;
import static com.inmobi.adserve.channels.util.InspectorStrings.PROCESSING_ERROR;
import static com.inmobi.adserve.channels.util.InspectorStrings.TERMINATED_REQUESTS;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ExceptionBlock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpRequestHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestHandler.class);
    public ResponseSender responseSender;
    private final Servlet servlet;
    private Marker traceMarker;
    private HttpRequest httpRequest;

    public String getTerminationReason() {
        return responseSender.getTerminationReason();
    }

    public void setTerminationReason(final String terminationReason) {
        responseSender.setTerminationReason(terminationReason);
    }

    @Inject
    public HttpRequestHandler(final Provider<Marker> traceMarkerProvider, final Servlet servlet,
            final ResponseSender responseSender) {
        if (null != traceMarkerProvider) {
            traceMarker = traceMarkerProvider.get();
        }
        this.servlet = servlet;
        this.responseSender = responseSender;
    }

    // Invoked when message is received over the connection
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final RequestParameterHolder requestParameterHolder = (RequestParameterHolder) msg;
        try {
            responseSender.setTerminationReason(requestParameterHolder.getTerminationReason());
            responseSender.setSasParams(requestParameterHolder.getSasParams());
            responseSender.casInternalRequestParameters = requestParameterHolder.getCasInternalRequestParameters();
            httpRequest = requestParameterHolder.getHttpRequest();
            LOG.debug(traceMarker, "Got the servlet {} , uri {}", servlet.getName(), httpRequest.getUri());
            servlet.handleRequest(this, new QueryStringDecoder(httpRequest.getUri()), ctx.channel());
        } catch (final Exception exception) {
            responseSender.setTerminationReason(CasConfigUtil.PROCESSING_ERROR);
            InspectorStats.incrementStatCount(TERMINATED_REQUESTS, PROCESSING_ERROR);
            responseSender.sendNoAdResponse(ctx.channel());
            final String exceptionClass = exception.getClass().getSimpleName();
            InspectorStats.incrementStatCount(exceptionClass, COUNT);
            LOG.error(traceMarker, "Error caught", exception);
            final String message = "stack trace is -> " + ExceptionBlock.getCustomStackTrace(exception);
            LOG.error(traceMarker, message);
        } finally {
            requestParameterHolder.getHttpRequest().release();
        }

    }

    public boolean isRequestFromLocalHost() {
        final String host = CasUtils.getHost(httpRequest);
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


}
