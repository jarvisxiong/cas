package com.inmobi.adserve.channels.server.handler;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.inject.Key;
import com.inmobi.adserve.channels.server.SimpleScope;


/**
 * @author abhishek.parwal
 * 
 */
@Sharable
public class TraceMarkerhandler extends SimpleChannelUpstreamHandler {

    public static final Marker TRACE_MAKER = MarkerFactory.getMarker("TRACE_MAKER");
    private final SimpleScope  scope;

    @Inject
    public TraceMarkerhandler(final SimpleScope scope) {
        this.scope = scope;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

        HttpRequest httpRequest = (HttpRequest) e.getMessage();

        boolean isTracer = Boolean.valueOf(httpRequest.getHeader("x-mkhoj-tracer"));
        Marker traceMarker = isTracer ? TRACE_MAKER : null;
        scope.enter();
        try {
            scope.seed(Key.get(Marker.class), traceMarker);
            ctx.sendUpstream(e);
        }
        finally {
            scope.exit();
        }

    }
}
