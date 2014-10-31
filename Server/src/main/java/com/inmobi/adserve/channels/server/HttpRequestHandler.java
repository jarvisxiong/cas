package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class HttpRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestHandler.class);

    public ResponseSender responseSender;

    private final Marker traceMarker;
    private final Servlet servlet;

    private HttpRequest httpRequest;

    public String getTerminationReason() {
        return responseSender.getTerminationReason();
    }

    public void setTerminationReason(final String terminationReason) {
        responseSender.setTerminationReason(terminationReason);
    }

    @Inject
    public HttpRequestHandler(@Nullable final Marker traceMarker, final Servlet servlet,
            final ResponseSender responseSender) {
        this.traceMarker = traceMarker;
        this.servlet = servlet;
        this.responseSender = responseSender;
    }

    // Invoked when message is received over the connection
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final RequestParameterHolder requestParameterHolder = (RequestParameterHolder) msg;
        try {
            responseSender.setTerminationReason(requestParameterHolder.getTerminationReason());
            responseSender.sasParams = requestParameterHolder.getSasParams();
            responseSender.casInternalRequestParameters = requestParameterHolder.getCasInternalRequestParameters();
            httpRequest = requestParameterHolder.getHttpRequest();

            LOG.debug(traceMarker, "Got the servlet {} , uri {}", servlet.getName(), httpRequest.getUri());

            servlet.handleRequest(this, new QueryStringDecoder(httpRequest.getUri()), ctx.channel());
        } catch (final Exception exception) {
            responseSender.setTerminationReason(CasConfigUtil.PROCESSING_ERROR);
            InspectorStats.incrementStatCount(InspectorStrings.PROCESSING_ERROR, InspectorStrings.COUNT);
            responseSender.sendNoAdResponse(ctx.channel());
            final String exceptionClass = exception.getClass().getSimpleName();
            InspectorStats.incrementStatCount(exceptionClass, InspectorStrings.COUNT);
            LOG.info(traceMarker, "stack trace is {}", exception);
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
