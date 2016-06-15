package com.inmobi.adserve.channels.server.servlet;

import static com.inmobi.adserve.channels.api.SASParamsUtils.isDeeplinkingSupported;

import java.util.Arrays;
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
import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
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
@Path("/rtbdFill")
public class ServletRtbd extends BaseServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletRtbd.class);

    @Inject
    public ServletRtbd(final Provider<Marker> traceMarkerProvider, final MatchSegments matchSegments,
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
        LOG.debug(traceMarker, "Inside RTBD servlet");
        InspectorStats.incrementStatCount(InspectorStrings.RULE_ENGINE_REQUESTS);
        super.handleRequest(hrh, queryStringDecoder, serverChannel);
    }

    @Override
    protected void specificEnrichment(final CasContext casContext, final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternal) {
        LOG.debug("enrichDstSpecific RTBD");
        casInternal.setBlockedIabCategories(getBlockedIabCategories(sasParams.getSiteId()));
        casInternal.setBlockedAdvertisers(getBlockedAdvertisers(sasParams.getSiteId()));
        sasParams.setDeeplinkingSupported(isDeeplinkingSupported(sasParams));

        final int bidFloorPercent = CasConfigUtil.getServerConfig().getInt("rtb.bidFloorPercent", 100);
        // SasParams SiteFloor has Math.max(tObject.site.ecpmFloor, tObject.site.cpmFloor)
        double auctionBidFloor = sasParams.getSiteFloor();
        auctionBidFloor = auctionBidFloor * bidFloorPercent / 100;
        sasParams.setMarketRate(Math.max(sasParams.getMarketRate(), auctionBidFloor));
        casInternal.setAuctionBidFloor(auctionBidFloor);

        final double tempDemandDensity = CasConfigUtil.getServerConfig().getDouble("rtb.demandDensity");
        final double tempLongTermRevenue = CasConfigUtil.getServerConfig().getDouble("rtb.longTermRevenue");

        /*
            Setting auction clearing price mechanics.
            Here demandDensity & longTermRevenue are equivalent to demandDensity*bidGuidance &
            longTermRevenue*bidGuidance respectively
         */
        casInternal.setDemandDensity(tempDemandDensity * sasParams.getMarketRate());
        casInternal.setLongTermRevenue(tempLongTermRevenue * sasParams.getMarketRate());
        casInternal.setPublisherYield(CasConfigUtil.getServerConfig().getInt("rtb.publisherYield"));

        /*
            demandDensity must always be <= longTermRevenue and publisherYield >= 1
            Capping
                demandDensity to longTermRevenue if demandDensity > longTermRevenue and
                publisherYield to 1 if publisherYield < 1
            as these parameters are adjustable via servlet
         */
        if (casInternal.getDemandDensity() > casInternal.getLongTermRevenue()) {
            casInternal.setDemandDensity(casInternal.getLongTermRevenue());
        }
        if (casInternal.getPublisherYield() < 1) {
            casInternal.setPublisherYield(1);
        }

        LOG.debug("BlockedCategories are {}", casInternal.getBlockedIabCategories());
        LOG.debug("BlockedAdvertisers are {}", casInternal.getBlockedAdvertisers());
        LOG.debug("Deeplinking supported: {}", sasParams.isDeeplinkingSupported());

        LOG.debug("rtb.bidFloorPercent is {}", bidFloorPercent);
        LOG.debug("rtb.demandDensity is {}", tempDemandDensity);
        LOG.debug("rtb.longTermRevenue is {}", tempLongTermRevenue);
        LOG.debug("rtb.publisherYield is {}", casInternal.getPublisherYield());
        LOG.debug("AuctionBidFloor is {}", auctionBidFloor);
    }

    @Override
    public String getName() {
        return "rtbdFill";
    }

    @Override
    protected boolean isEnabled() {
        return CasConfigUtil.getServerConfig().getBoolean("isRtbEnabled", true);
    }

    private static List<String> getBlockedAdvertisers(final String siteId) {
        List<String> blockedAdvertisers = null;
        if (null != siteId) {
            final SiteFilterEntity siteFilterEntity =
                    CasConfigUtil.repositoryHelper.querySiteFilterRepository(siteId, 6);
            if (null != siteFilterEntity && siteFilterEntity.getBlockedAdvertisers() != null) {
                blockedAdvertisers = Arrays.asList(siteFilterEntity.getBlockedAdvertisers());
            }
        }
        return blockedAdvertisers;
    }

}
