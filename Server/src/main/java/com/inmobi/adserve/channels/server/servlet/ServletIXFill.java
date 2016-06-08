package com.inmobi.adserve.channels.server.servlet;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.requesthandler.filters.IXAdGroupLevelFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.IxAdvertiserLevelFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/ixFill")
public class ServletIXFill extends BaseServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletIXFill.class);

    @Inject
    public ServletIXFill(final Provider<Marker> traceMarkerProvider, final MatchSegments matchSegments,
            final RequestFilters requestFilters, final ChannelSegmentFilterApplier channelSegmentFilterApplier,
            final CasUtils casUtils, final AsyncRequestMaker asyncRequestMaker,
            @IxAdvertiserLevelFilters final List<AdvertiserLevelFilter> advertiserLevelFilters,
            @IXAdGroupLevelFilters final List<AdGroupLevelFilter> adGroupLevelFilters) {
        super(matchSegments, traceMarkerProvider, channelSegmentFilterApplier, casUtils, requestFilters,
                asyncRequestMaker, advertiserLevelFilters, adGroupLevelFilters);
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        final Marker traceMarker = traceMarkerProvider.get();
        if (LOG.isDebugEnabled()) {
            LOG.debug(traceMarker, "Inside Servlet {}", this.getClass().getSimpleName());
        }
        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        super.handleRequest(hrh, queryStringDecoder, serverChannel);
    }

    @Override
    protected void specificEnrichment(final CasContext casContext, final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternal) {
        LOG.debug("enrichDstSpecific IX");
        casInternal.setAuctionBidFloor(sasParams.getSiteFloor());
        sasParams.setMarketRate(Math.max(sasParams.getMarketRate(), casInternal.getAuctionBidFloor()));
        LOG.debug("Final ixFloor is {}", casInternal.getAuctionBidFloor());
        LOG.debug("Final ixMarketRate is {}", sasParams.getMarketRate());
    }

    @Override
    public String getName() {
        return "ixFill";
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }
}
