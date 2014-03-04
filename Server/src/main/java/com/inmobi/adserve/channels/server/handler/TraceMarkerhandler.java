package com.inmobi.adserve.channels.server.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

import javax.inject.Inject;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.SimpleScope;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
@Singleton
public class TraceMarkerhandler extends SimpleChannelInboundHandler<HttpRequest> {

    public static final Marker TRACE_MAKER = MarkerFactory.getMarker("TRACE_MAKER");
    private final SimpleScope  scope;

    @Inject
    public TraceMarkerhandler(final SimpleScope scope) {
        this.scope = scope;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpRequest httpRequest) {

        boolean isTracer = Boolean.valueOf(httpRequest.headers().get("x-mkhoj-tracer"));
        Marker traceMarker = isTracer ? TRACE_MAKER : null;
        scope.enter();
        try {
            scope.seed(Key.get(Marker.class), traceMarker);
            ctx.fireChannelRead(httpRequest);
        }
        finally {
            scope.exit();
        }

    }

}
