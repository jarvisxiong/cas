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
import com.inmobi.adserve.channels.server.requesthandler.filters.DcpAndRtbAdGroupLevelFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.DcpAndRtbdAdvertiserLevelFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/backfill")
public class ServletBackFill extends BaseServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletBackFill.class);


    @Inject
    public ServletBackFill(final Provider<Marker> traceMarkerProvider, final MatchSegments matchSegments,
            final RequestFilters requestFilters, final ChannelSegmentFilterApplier channelSegmentFilterApplier,
            final CasUtils casUtils, final AsyncRequestMaker asyncRequestMaker,
            @DcpAndRtbdAdvertiserLevelFilters final List<AdvertiserLevelFilter> advertiserLevelFilters,
            @DcpAndRtbAdGroupLevelFilters final List<AdGroupLevelFilter> adGroupLevelFilters) {
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
        InspectorStats.incrementStatCount(InspectorStrings.BACK_FILL_REQUESTS);
        super.handleRequest(hrh, queryStringDecoder, serverChannel);
    }

    @Override
    protected void specificEnrichment(final CasContext casContext, final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternal) {
        LOG.debug("enrichDstSpecific DCP");
        casInternal.setBlockedIabCategories(getBlockedIabCategories(sasParams.getSiteId()));
        // SasParams SiteFloor has Math.max(tObject.site.ecpmFloor, tObject.site.cpmFloor)
        // This is currently only being used by DCPRubicon
        final double auctionBidFloor = sasParams.getSiteFloor();
        casInternal.setAuctionBidFloor(auctionBidFloor);
        LOG.debug("BlockedCategories are {}", casInternal.getBlockedIabCategories());
        LOG.debug("AuctionBidFloor is {}", auctionBidFloor);
    }

    @Override
    public String getName() {
        return "BackFill";
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }
}
