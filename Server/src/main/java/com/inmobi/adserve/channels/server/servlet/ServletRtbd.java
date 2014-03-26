package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
@Path("/rtbdFill")
public class ServletRtbd implements Servlet {
    private static final Logger               LOG = LoggerFactory.getLogger(ServletRtbd.class);
    private final MatchSegments               matchSegments;
    private final Provider<Marker>            traceMarkerProvider;
    private final RequestFilters              requestFilters;
    private final ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private final CasUtils                    casUtils;
    private final AsyncRequestMaker           asyncRequestMaker;

    @Inject
    public ServletRtbd(final Provider<Marker> traceMarkerProvider, final MatchSegments matchSegments,
            final RequestFilters requestFilters, final ChannelSegmentFilterApplier channelSegmentFilterApplier,
            final CasUtils casUtils, final AsyncRequestMaker asyncRequestMaker) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.matchSegments = matchSegments;
        this.requestFilters = requestFilters;
        this.channelSegmentFilterApplier = channelSegmentFilterApplier;
        this.casUtils = casUtils;
        this.asyncRequestMaker = asyncRequestMaker;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        Marker traceMarker = traceMarkerProvider.get();
        LOG.debug(traceMarker, "Inside RTBD servlet");
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        InspectorStats.incrementStatCount(InspectorStrings.ruleEngineRequests);
        ServletBackFill servletBackFill = new ServletBackFill(matchSegments, traceMarkerProvider,
                channelSegmentFilterApplier, casUtils, requestFilters, asyncRequestMaker);
        servletBackFill.handleRequest(hrh, queryStringDecoder, serverChannel);
    }

    @Override
    public String getName() {
        return "rtbdFill";
    }
}
