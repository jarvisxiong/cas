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
import org.slf4j.MarkerFactory;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.NettyRequestScope;
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

    public static final Marker         TRACE_MAKER = MarkerFactory.getMarker("TRACE_MAKER");

    private final NettyRequestScope    scope;
    private final Map<String, Servlet> pathToServletMap;
    private final ServletInvalid       invalidServlet;

    @Inject
    public NettyRequestScopeSeedHandler(final NettyRequestScope scope, final Map<String, Servlet> pathToServletMap,
            final ServletInvalid invalidServlet) {
        this.scope = scope;
        this.pathToServletMap = pathToServletMap;
        this.invalidServlet = invalidServlet;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        HttpRequest httpRequest = (HttpRequest) msg;
        boolean isTracer = Boolean.valueOf(httpRequest.headers().get("x-mkhoj-tracer"));
        Marker traceMarker = isTracer ? TRACE_MAKER : null;
        scope.enter();
        try {
            scope.seed(Key.get(Marker.class), traceMarker);
            scope.seed(Key.get(ResponseSender.class), new ResponseSender());

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
            String path = queryStringDecoder.path();

            Servlet servlet = pathToServletMap.get(path);
            if (servlet == null) {
                servlet = invalidServlet;
            }

            log.debug("Request servlet is {} for path {}", servlet, path);
            scope.seed(Servlet.class, servlet);

            ctx.fireChannelRead(httpRequest);
        }
        finally {
            scope.exit();
        }
    }

}
