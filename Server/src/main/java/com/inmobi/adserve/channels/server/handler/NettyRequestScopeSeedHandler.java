package com.inmobi.adserve.channels.server.handler;

import static com.inmobi.adserve.channels.util.InspectorStrings.INVALID_SERVLET_REQUEST;
import static com.inmobi.adserve.channels.util.InspectorStrings.TERMINATED_REQUESTS;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.servlet.ServletInvalid;
import com.inmobi.adserve.channels.util.InspectorStats;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;


/**
 * @author abhishek.parwal
 *
 */
@Sharable
@Singleton
@Slf4j
public class NettyRequestScopeSeedHandler extends ChannelInboundHandlerAdapter {
    private final NettyRequestScope scope;
    private final Map<String, Servlet> pathToServletMap;
    private final ServletInvalid invalidServlet;

    @Inject
    public NettyRequestScopeSeedHandler(final NettyRequestScope scope, final Map<String, Servlet> pathToServletMap,
            final ServletInvalid invalidServlet) {
        this.scope = scope;
        this.pathToServletMap = pathToServletMap;
        this.invalidServlet = invalidServlet;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final HttpRequest httpRequest = (HttpRequest) msg;
        final boolean isTracer = Boolean.valueOf(httpRequest.headers().get("x-mkhoj-tracer"));
        final Marker traceMarker = isTracer ? NettyRequestScope.TRACE_MAKER : null;
        scope.enter();
        try {
            scope.seed(Marker.class, traceMarker);
            scope.seed(ResponseSender.class, new ResponseSender());

            final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
            final String path = queryStringDecoder.path();
            Servlet servlet = pathToServletMap.get(path);
            if (servlet == null) {
                if (log.isDebugEnabled()) {
                    log.debug(traceMarker, "Length of Request URI -> {}", httpRequest.getUri().length());
                    log.debug(traceMarker, "Invalid Servlet Requested -> {}", httpRequest.getUri());
                }
                InspectorStats.incrementStatCount(TERMINATED_REQUESTS, INVALID_SERVLET_REQUEST);
                servlet = invalidServlet;
            }
            log.debug("Request servlet is {} for path {}", servlet, path);
            scope.seed(Servlet.class, servlet);

            ctx.fireChannelRead(httpRequest);
        } finally {
            scope.exit();
        }
    }

}
