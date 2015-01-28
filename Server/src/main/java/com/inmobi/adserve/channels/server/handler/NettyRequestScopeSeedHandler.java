package com.inmobi.adserve.channels.server.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Map;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.servlet.ServletInvalid;


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
    private final Provider<Marker> traceMarkerProvider;


    @Inject
    public NettyRequestScopeSeedHandler(final NettyRequestScope scope, final Map<String, Servlet> pathToServletMap,
            final ServletInvalid invalidServlet, final Provider<Marker> traceMarkerProvider) {
        this.scope = scope;
        this.pathToServletMap = pathToServletMap;
        this.invalidServlet = invalidServlet;
        this.traceMarkerProvider = traceMarkerProvider;
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
