package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Singleton
@Path("/rtbdFill")
public class ServletRtbd implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletRtbd.class);
    private final MatchSegments matchSegments;
    private final Provider<Marker> traceMarkerProvider;
    private final RequestFilters requestFilters;


    @Inject
    public ServletRtbd(final Provider<Marker> traceMarkerProvider, MatchSegments matchSegments,
                       final RequestFilters requestFilters) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.matchSegments = matchSegments;
        this.requestFilters = requestFilters;
    }

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e
            ) throws Exception {
        Marker traceMarker = traceMarkerProvider.get();
        LOG.debug(traceMarker, "Inside RTBD servlet");
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        InspectorStats.incrementStatCount(InspectorStrings.ruleEngineRequests);
        ServletBackFill servletBackFill = new ServletBackFill(matchSegments, traceMarkerProvider, requestFilters);
        servletBackFill.handleRequest(hrh, queryStringDecoder, e);
    }

    @Override
    public String getName() {
        return "rtbdFill";
    }
}
