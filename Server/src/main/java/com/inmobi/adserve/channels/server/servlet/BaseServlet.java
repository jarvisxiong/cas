package com.inmobi.adserve.channels.server.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender.ResponseFormat;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


public abstract class BaseServlet implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(BaseServlet.class);
    private static final double MIN_RTB_FLOOR = 0.05;
    protected final Provider<Marker> traceMarkerProvider;

    private final MatchSegments matchSegments;
    private final ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private final RequestFilters requestFilters;
    private final AsyncRequestMaker asyncRequestMaker;
    private final List<AdvertiserLevelFilter> advertiserLevelFilters;
    private final List<AdGroupLevelFilter> adGroupLevelFilters;
    protected final CasUtils casUtils;
    protected SASRequestParameters sasParams;
    protected CasInternalRequestParameters casInternal;

    protected BaseServlet(final MatchSegments matchSegments, final Provider<Marker> traceMarkerProvider,
            final ChannelSegmentFilterApplier channelSegmentFilterApplier, final CasUtils casUtils,
            final RequestFilters requestFilters, final AsyncRequestMaker asyncRequestMaker,
            final List<AdvertiserLevelFilter> advertiserLevelFilters, final List<AdGroupLevelFilter> adGroupLevelFilters) {
        this.matchSegments = matchSegments;
        this.traceMarkerProvider = traceMarkerProvider;
        this.channelSegmentFilterApplier = channelSegmentFilterApplier;
        this.casUtils = casUtils;
        this.requestFilters = requestFilters;
        this.asyncRequestMaker = asyncRequestMaker;
        this.advertiserLevelFilters = advertiserLevelFilters;
        this.adGroupLevelFilters = adGroupLevelFilters;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        final CasContext casContext = new CasContext();
        final Marker traceMarker = traceMarkerProvider.get();
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        sasParams = hrh.responseSender.getSasParams();
        casInternal = hrh.responseSender.casInternalRequestParameters;
        casInternal.setTraceEnabled(Boolean.valueOf(hrh.getHttpRequest().headers().get("x-mkhoj-tracer")));

        // Send NO_AD response, if not enabled.
        if (!isEnabled()) {
            LOG.debug("Servlet {} is disabled via server config. Sending NO_AD response.", getName());
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        if (requestFilters.isDroppedInRequestFilters(hrh)) {
            LOG.debug("Request is dropped in request filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        // getting the selected third party site details
        final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails = matchSegments.matchSegments(sasParams);
        if (CollectionUtils.isEmpty(matchedSegmentDetails)) {
            LOG.debug(traceMarker, "No Entities matching the request.");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        // applying all the filters
        final List<ChannelSegment> filteredSegments =
                channelSegmentFilterApplier.getChannelSegments(matchedSegmentDetails, sasParams, casContext,
                        advertiserLevelFilters, adGroupLevelFilters);

        if (filteredSegments == null || filteredSegments.isEmpty()) {
            LOG.debug(traceMarker, "All segments dropped in filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        // Incrementing Adapter Specific Total Selected Segments Stats
        incrementTotalSelectedSegmentStats(filteredSegments);

        commonEnrichment(hrh);
        specificEnrichment(hrh, casContext);
        hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternal;
        hrh.responseSender.getAuctionEngine().sasParams = sasParams;

        LOG.debug("Total channels available for sending requests {}", filteredSegments.size());
        final List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
        List<ChannelSegment> dcpSegments;

        dcpSegments =
                asyncRequestMaker.prepareForAsyncRequest(filteredSegments, CasConfigUtil.getServerConfig(),
                        CasConfigUtil.getRtbConfig(), CasConfigUtil.getAdapterConfig(), hrh.responseSender,
                        sasParams.getUAdapters(), serverChannel, CasConfigUtil.repositoryHelper, sasParams,
                        casInternal, rtbSegments);

        LOG.debug("rtb rankList size is {}", rtbSegments.size());
        if (CollectionUtils.isEmpty(dcpSegments) && CollectionUtils.isEmpty(rtbSegments)) {
            LOG.debug("No successful configuration of adapter ");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        final List<ChannelSegment> tempRankList =
                asyncRequestMaker.makeAsyncRequests(dcpSegments, serverChannel, rtbSegments);

        hrh.responseSender.setRankList(tempRankList);
        hrh.responseSender.getAuctionEngine().setUnfilteredChannelSegmentList(rtbSegments);
        LOG.debug(traceMarker, "Number of tpans whose request was successfully completed {}", hrh.responseSender
                .getRankList().size());
        LOG.debug(traceMarker, "Number of rtb tpans whose request was successfully completed {}", hrh.responseSender
                .getAuctionEngine().getUnfilteredChannelSegmentList().size());
        // if none of the async request succeed, we return "NO_AD"
        if (hrh.responseSender.getRankList().isEmpty()
                && hrh.responseSender.getAuctionEngine().getUnfilteredChannelSegmentList().isEmpty()) {
            LOG.debug(traceMarker, "No calls");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        if (hrh.responseSender.getAuctionEngine().areAllChannelSegmentRequestsComplete()) {
            final AdNetworkInterface highestBid = hrh.responseSender.getAuctionEngine().runAuctionEngine();
            if (null != highestBid) {
                LOG.debug(traceMarker, "Sending rtb response of {}", highestBid.getName());
                hrh.responseSender.sendAdResponse(highestBid, serverChannel);
                // highestBid.impressionCallback();
                return;
            }
            // Resetting the rankIndexToProcess for already completed adapters.
            hrh.responseSender.processDcpList(serverChannel);
            LOG.debug(traceMarker, "returned from send Response, ranklist size is {}", hrh.responseSender.getRankList()
                    .size());
        }
    }

    /**
     * Enrichment common for all ad pools
     * 
     * @param hrh
     */
    protected void commonEnrichment(final HttpRequestHandler hrh) {
        // Setting isResponseOnlyFromDCP from config
        final boolean isResponseOnlyFromDcp = CasConfigUtil.getServerConfig().getBoolean("isResponseOnyFromDCP", false);
        LOG.debug("isResponseOnlyFromDcp from config is {}", isResponseOnlyFromDcp);
        sasParams.setResponseOnlyFromDcp(isResponseOnlyFromDcp);

        // Set imai content if r-format is imai
        String imaiBaseUrl = null;
        if (hrh.responseSender.getResponseFormat() == ResponseFormat.IMAI) {
            if (sasParams.getOsId() == 3) {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("androidBaseUrl");
            } else {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("iPhoneBaseUrl");
            }
        }
        sasParams.setImaiBaseUrl(imaiBaseUrl);
        LOG.debug("imai base url is {}", imaiBaseUrl);

        final SiteMetaDataEntity siteMetaDataEntity =
                matchSegments.getRepositoryHelper().querySiteMetaDetaRepository(sasParams.getSiteId());
        if (null != siteMetaDataEntity) {
            casInternal.setSiteAccountType(siteMetaDataEntity.getAccountTypesAllowed());
        }

        casInternal.setSiteAccountType(AccountType.SELF_SERVE);
        casInternal.setAuctionId(ImpressionIdGenerator.getInstance().getImpressionId(sasParams.getSiteIncId()));
        LOG.debug("Auction id generated is {}", casInternal.getAuctionId());
    }

    /**
     * Applicable for RTBD and DCP. For IX we have overridden method
     * 
     * @param hrh
     * @param casContext
     */
    protected void specificEnrichment(final HttpRequestHandler hrh, final CasContext casContext) {
        LOG.debug("Base class enrichRequest");
        casInternal.setBlockedIabCategories(getBlockedIabCategories(hrh));
        casInternal.setBlockedAdvertisers(getBlockedAdvertisers(hrh));

        final double networkSiteEcpm = casUtils.getNetworkSiteEcpm(casContext, sasParams);
        final double segmentFloor = casUtils.getRtbFloor(casContext);
        final double siteFloor = sasParams.getSiteFloor();
        final double auctionBidFloor =
                hrh.responseSender.getAuctionEngine().calculateAuctionFloor(siteFloor, segmentFloor, MIN_RTB_FLOOR,
                        networkSiteEcpm);
        casInternal.setAuctionBidFloor(auctionBidFloor);

        LOG.debug("RTB floor from the pricing engine entity is {}", segmentFloor);
        LOG.debug("BlockedCategories are {}", casInternal.getBlockedIabCategories());
        LOG.debug("BlockedAdvertisers are {}", casInternal.getBlockedAdvertisers());
        LOG.debug("Site floor is {}", siteFloor);
        LOG.debug("NetworkSiteEcpm is {}", networkSiteEcpm);
        LOG.debug("SegmentFloor is {}", segmentFloor);
        LOG.debug("Minimum rtb floor is {}", MIN_RTB_FLOOR);
        LOG.debug("Final rtbFloor is {}", casInternal.getAuctionBidFloor());
    }

    private List<String> getBlockedIabCategories(final HttpRequestHandler hrh) {
        List<String> blockedCategories = null;
        if (null != sasParams.getSiteId()) {
            final SiteFilterEntity siteFilterEntity =
                    CasConfigUtil.repositoryHelper.querySiteFilterRepository(sasParams.getSiteId(), 4);
            if (null != siteFilterEntity && siteFilterEntity.getBlockedIabCategories() != null) {
                blockedCategories = Arrays.asList(siteFilterEntity.getBlockedIabCategories());
            }
        }
        return blockedCategories;
    }

    private List<String> getBlockedAdvertisers(final HttpRequestHandler hrh) {
        List<String> blockedAdvertisers = null;
        if (null != sasParams.getSiteId()) {
            final SiteFilterEntity siteFilterEntity =
                    CasConfigUtil.repositoryHelper.querySiteFilterRepository(sasParams.getSiteId(), 6);
            if (null != siteFilterEntity && siteFilterEntity.getBlockedAdvertisers() != null) {
                blockedAdvertisers = Arrays.asList(siteFilterEntity.getBlockedAdvertisers());
            }
        }
        return blockedAdvertisers;
    }

    @Override
    public String getName() {
        return null;
    }

    protected abstract boolean isEnabled();


    /**
     * @param channelSegment
     */
    private void incrementTotalSelectedSegmentStats(final List<ChannelSegment> filteredSegments) {
        for (final ChannelSegment channelSegment : filteredSegments) {
            channelSegment.incrementInspectorStats(InspectorStrings.TOTAL_SELECTED_SEGMENTS);
        }
    }
}
