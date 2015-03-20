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
import com.inmobi.casthrift.DemandSourceType;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


public abstract class BaseServlet implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(BaseServlet.class);
    private static final double MIN_RTB_FLOOR = 0.05;
    protected final Provider<Marker> traceMarkerProvider;

    private final MatchSegments matchSegments;
    private final ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private final CasUtils casUtils;
    private final RequestFilters requestFilters;
    private final AsyncRequestMaker asyncRequestMaker;
    private final List<AdvertiserLevelFilter> advertiserLevelFilters;
    private final List<AdGroupLevelFilter> adGroupLevelFilters;

    BaseServlet(final MatchSegments matchSegments, final Provider<Marker> traceMarkerProvider,
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
        final SASRequestParameters sasParams = hrh.responseSender.getSasParams();

        // Send NO_AD response, if not enabled.
        if (!isEnabled()) {
            LOG.debug("Servlet {} is disabled via server config. Sending NO_AD response.", getName());
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.getSasParams();
        final CasInternalRequestParameters casInternalRequestParametersGlobal =
                hrh.responseSender.casInternalRequestParameters;

        casInternalRequestParametersGlobal.setTraceEnabled(Boolean.valueOf(hrh.getHttpRequest().headers()
                .get("x-mkhoj-tracer")));

        if (requestFilters.isDroppedInRequestFilters(hrh)) {
            LOG.debug("Request is dropped in request filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        // Setting isResponseOnlyFromDCP from config
        final boolean isResponseOnlyFromDcp = CasConfigUtil.getServerConfig().getBoolean("isResponseOnyFromDCP", false);
        LOG.debug("isResponseOnlyFromDcp from config is {}", isResponseOnlyFromDcp);
        sasParams.setResponseOnlyFromDcp(isResponseOnlyFromDcp);

        // Setting isVideoSupported based on the Request params. Only supported for IX request.
        if (DemandSourceType.IX == DemandSourceType.findByValue(sasParams.getDst())) {
            final boolean isVideoSupported = casUtils.isVideoSupported(sasParams);
            LOG.debug("isVideoSupported for this request is {}", isVideoSupported);
            sasParams.setVideoSupported(isVideoSupported);
        }

        // Set imai content if r-format is imai
        String imaiBaseUrl = null;
        if (hrh.responseSender.getResponseFormat() == ResponseFormat.IMAI) {
            if (hrh.responseSender.getSasParams().getOsId() == 3) {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("androidBaseUrl");
            } else {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("iPhoneBaseUrl");
            }
        }
        hrh.responseSender.getSasParams().setImaiBaseUrl(imaiBaseUrl);
        LOG.debug("imai base url is {}", hrh.responseSender.getSasParams().getImaiBaseUrl());

        // getting the selected third party site details
        final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails =
                matchSegments.matchSegments(hrh.responseSender.getSasParams());

        if (CollectionUtils.isEmpty(matchedSegmentDetails)) {
            LOG.debug(traceMarker, "No Entities matching the request.");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        final SiteMetaDataEntity siteMetaDataEntity =
                matchSegments.getRepositoryHelper().querySiteMetaDetaRepository(sasParams.getSiteId());
        casInternalRequestParametersGlobal.setSiteAccountType(AccountType.SELF_SERVE);
        if (null != siteMetaDataEntity) {
            casInternalRequestParametersGlobal.setSiteAccountType(siteMetaDataEntity.getAccountTypesAllowed());
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
        for (ChannelSegment channelSegment : filteredSegments) {
            incrementTotalSelectedSegmentStats(channelSegment);
        }

        final double networkSiteEcpm = casUtils.getNetworkSiteEcpm(casContext, sasParams);
        final double segmentFloor = casUtils.getRtbFloor(casContext);
        enrichCasInternalRequestParameters(hrh, filteredSegments, networkSiteEcpm, segmentFloor);
        sasParams.setMarketRate(Math.max(sasParams.getMarketRate(),
                casInternalRequestParametersGlobal.getAuctionBidFloor()));
        hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

        LOG.debug("Total channels available for sending requests {}", filteredSegments.size());
        final List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
        List<ChannelSegment> dcpSegments;

        dcpSegments =
                asyncRequestMaker.prepareForAsyncRequest(filteredSegments, CasConfigUtil.getServerConfig(),
                        CasConfigUtil.getRtbConfig(), CasConfigUtil.getAdapterConfig(), hrh.responseSender,
                        sasParams.getUAdapters(), serverChannel, CasConfigUtil.repositoryHelper,
                        hrh.responseSender.getSasParams(), casInternalRequestParametersGlobal, rtbSegments);

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

    private void enrichCasInternalRequestParameters(final HttpRequestHandler hrh,
            final List<ChannelSegment> filteredSegments, final double networkSiteEcpm,
            final double segmentFloor) {
        final CasInternalRequestParameters casInternalRequestParametersGlobal =
                hrh.responseSender.casInternalRequestParameters;
        casInternalRequestParametersGlobal.setHighestEcpm(getHighestEcpm(filteredSegments));
        casInternalRequestParametersGlobal.setBlockedIabCategories(getBlockedIabCategories(hrh));
        casInternalRequestParametersGlobal.setBlockedAdvertisers(getBlockedAdvertisers(hrh));
        final double siteFloor = hrh.responseSender.getSasParams().getSiteFloor();
        final double auctionBidFloor =
                hrh.responseSender.getAuctionEngine().calculateAuctionFloor(siteFloor, segmentFloor, MIN_RTB_FLOOR,
                        networkSiteEcpm);
        final long siteIncId = hrh.responseSender.getSasParams().getSiteIncId();
        casInternalRequestParametersGlobal.setAuctionBidFloor(auctionBidFloor);
        casInternalRequestParametersGlobal.setAuctionId(ImpressionIdGenerator.getInstance().getImpressionId(siteIncId));
        LOG.debug("RTB floor from the pricing engine entity is {}", segmentFloor);
        LOG.debug("Highest Ecpm is {}", casInternalRequestParametersGlobal.getHighestEcpm());
        LOG.debug("BlockedCategories are {}", casInternalRequestParametersGlobal.getBlockedIabCategories());
        LOG.debug("BlockedAdvertisers are {}", casInternalRequestParametersGlobal.getBlockedAdvertisers());
        LOG.debug("Site floor is {}", siteFloor);
        LOG.debug("NetworkSiteEcpm is {}", networkSiteEcpm);
        LOG.debug("SegmentFloor is {}", segmentFloor);
        LOG.debug("Minimum rtb floor is {}", MIN_RTB_FLOOR);
        LOG.debug("Final rtbFloor is {}", casInternalRequestParametersGlobal.getAuctionBidFloor());
        LOG.debug("Auction id generated is {}", casInternalRequestParametersGlobal.getAuctionId());
    }

    private double getHighestEcpm(final List<ChannelSegment> channelSegments) {
        double highestEcpm = 0;
        for (final ChannelSegment channelSegment : channelSegments) {
            if (channelSegment.getChannelSegmentFeedbackEntity().getECPM() < 10.0
                    && highestEcpm < channelSegment.getChannelSegmentFeedbackEntity().getECPM()) {
                highestEcpm = channelSegment.getChannelSegmentFeedbackEntity().getECPM();
            }
        }
        return highestEcpm;
    }

    private List<String> getBlockedIabCategories(final HttpRequestHandler hrh) {
        List<String> blockedCategories = null;
        if (null != hrh.responseSender.getSasParams().getSiteId()) {
            final SiteFilterEntity siteFilterEntity =
                    CasConfigUtil.repositoryHelper.querySiteFilterRepository(hrh.responseSender.getSasParams()
                            .getSiteId(), 4);
            if (null != siteFilterEntity && siteFilterEntity.getBlockedIabCategories() != null) {
                blockedCategories = Arrays.asList(siteFilterEntity.getBlockedIabCategories());
            }
        }
        return blockedCategories;
    }

    private List<String> getBlockedAdvertisers(final HttpRequestHandler hrh) {
        List<String> blockedAdvertisers = null;
        if (null != hrh.responseSender.getSasParams().getSiteId()) {
            final SiteFilterEntity siteFilterEntity =
                    CasConfigUtil.repositoryHelper.querySiteFilterRepository(hrh.responseSender.getSasParams()
                            .getSiteId(), 6);
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

    protected abstract Logger getLogger();

    protected abstract boolean isEnabled();

    /**
     * @param channelSegment
     */
    private void incrementTotalSelectedSegmentStats(final ChannelSegment channelSegment) {
        channelSegment.incrementInspectorStats(InspectorStrings.TOTAL_SELECTED_SEGMENTS);
    }

}
