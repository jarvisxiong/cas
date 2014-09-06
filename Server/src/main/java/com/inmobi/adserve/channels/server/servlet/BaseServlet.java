package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.util.MetricsManager;
import com.inmobi.casthrift.DemandSourceType;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

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
import com.inmobi.adserve.channels.entity.PublisherFilterEntity;
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


public abstract class BaseServlet implements Servlet {
    private static final Logger               LOG = LoggerFactory.getLogger(BaseServlet.class);

    private final MatchSegments               matchSegments;
    protected final Provider<Marker>            traceMarkerProvider;
    private final ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private final CasUtils                    casUtils;
    private final RequestFilters              requestFilters;
    private final AsyncRequestMaker           asyncRequestMaker;
    private final List<AdvertiserLevelFilter> 	advertiserLevelFilters;
    private final List<AdGroupLevelFilter> 		adGroupLevelFilters;
    static List<String>                       	nativeSites        = new ArrayList<String>(Arrays.asList("69d6ab27d03f407f9f6fa9c5fad77afd","31588012724c4e8ea477c88d7d2b2e15","495362deeca64c52bd14e2108d34b4c2","7a2b63166a0f47bb98e3269c16e76fcd", "55b798bd8f1c4de5b89823fbacf419bc"));

    BaseServlet(final MatchSegments matchSegments, final Provider<Marker> traceMarkerProvider,
            final ChannelSegmentFilterApplier channelSegmentFilterApplier, final CasUtils casUtils,
            final RequestFilters requestFilters, final AsyncRequestMaker asyncRequestMaker, final List<AdvertiserLevelFilter> advertiserLevelFilters, final List<AdGroupLevelFilter> adGroupLevelFilters) {
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
        CasContext casContext = new CasContext();
        Marker traceMarker = traceMarkerProvider.get();
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        SASRequestParameters sasParams = hrh.responseSender.sasParams;

        //TODO: Review/Debug this line for NullPointerException
        MetricsManager.updateIncomingRequestsStats(DemandSourceType.findByValue(sasParams.getDst()).name(), sasParams.getCountryId(), sasParams.getCountryCode());

        hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;

        casInternalRequestParametersGlobal.traceEnabled = Boolean.valueOf(hrh.getHttpRequest().headers().get("x-mkhoj-tracer"));

        if (requestFilters.isDroppedInRequestFilters(hrh)) {
            LOG.debug("Request is dropped in request filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }
        if (nativeSites.contains(sasParams.getSiteId())) {
            InspectorStats.incrementStatCount(InspectorStrings.SITE_LEVEL_REQUEST + "-" + sasParams.getSiteId());
        }

        // Setting isResponseOnlyFromDCP from config
        boolean isResponseOnlyFromDcp = CasConfigUtil.getServerConfig().getBoolean("isResponseOnyFromDCP", false);
        LOG.debug("isResponseOnlyFromDcp from config is {}", isResponseOnlyFromDcp);
        sasParams.setResponseOnlyFromDcp(isResponseOnlyFromDcp);

        // Setting isBannerVideoSupported based on the Request params.
        boolean isBannerVideoSupported = casUtils.isBannerVideoSupported(sasParams);
        LOG.debug("isBannerVideoSupported for this request is {}", isBannerVideoSupported);
        sasParams.setBannerVideoSupported(isBannerVideoSupported);

        // Set imai content if r-format is imai
        String imaiBaseUrl = null;
        if (hrh.responseSender.getResponseFormat() == ResponseFormat.IMAI) {
            if (hrh.responseSender.sasParams.getOsId() == 3) {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("androidBaseUrl");
            }
            else {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("iPhoneBaseUrl");
            }
        }
        hrh.responseSender.sasParams.setImaiBaseUrl(imaiBaseUrl);
        LOG.debug("imai base url is {}", hrh.responseSender.sasParams.getImaiBaseUrl());

        // getting the selected third party site details
        List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails = matchSegments
                .matchSegments(hrh.responseSender.sasParams);

        if (CollectionUtils.isEmpty(matchedSegmentDetails)) {
            LOG.debug(traceMarker, "No Entities matching the request.");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        SiteMetaDataEntity siteMetaDataEntity = matchSegments.getRepositoryHelper().querySiteMetaDetaRepository(sasParams.getSiteId());
        casInternalRequestParametersGlobal.siteAccountType = AccountType.SELF_SERVE;
        if (null != siteMetaDataEntity) {
            casInternalRequestParametersGlobal.siteAccountType = siteMetaDataEntity.getAccountTypesAllowed();
        }

        // applying all the filters
        List<ChannelSegment> filteredSegments = channelSegmentFilterApplier.getChannelSegments(matchedSegmentDetails,
                sasParams, casContext, advertiserLevelFilters, adGroupLevelFilters);

        if (filteredSegments == null || filteredSegments.size() == 0) {
            LOG.debug(traceMarker, "All segments dropped in filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        double networkSiteEcpm = casUtils.getNetworkSiteEcpm(casContext, sasParams);
        double segmentFloor = casUtils.getRtbFloor(casContext, hrh.responseSender.sasParams);
        enrichCasInternalRequestParameters(hrh, filteredSegments, casInternalRequestParametersGlobal.auctionBidFloor,
                sasParams.getSiteFloor(), sasParams.getSiteIncId(), networkSiteEcpm, segmentFloor);
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

        LOG.debug("Total channels available for sending requests {}", filteredSegments.size());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
        List<ChannelSegment> dcpSegments;

        dcpSegments = asyncRequestMaker.prepareForAsyncRequest(filteredSegments, CasConfigUtil.getServerConfig(),
                CasConfigUtil.getRtbConfig(), CasConfigUtil.getAdapterConfig(), hrh.responseSender,
                sasParams.getUAdapters(), serverChannel, CasConfigUtil.repositoryHelper, hrh.responseSender.sasParams,
                casInternalRequestParametersGlobal, rtbSegments);

        LOG.debug("rtb rankList size is {}", rtbSegments.size());
        if (dcpSegments.isEmpty() && rtbSegments.isEmpty()) {
            LOG.debug("No successful configuration of adapter ");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        List<ChannelSegment> tempRankList = asyncRequestMaker
                .makeAsyncRequests(dcpSegments, serverChannel, rtbSegments);

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
            AdNetworkInterface highestBid = hrh.responseSender.getAuctionEngine().runAuctionEngine();
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
            final List<ChannelSegment> filteredSegments, final Double rtbdFloor, final Double siteFloor,
            final long siteIncId, final double networkSiteEcpm, final double segmentFloor) {
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;
        casInternalRequestParametersGlobal.highestEcpm = getHighestEcpm(filteredSegments);
        casInternalRequestParametersGlobal.blockedCategories = getBlockedCategories(hrh);
        casInternalRequestParametersGlobal.blockedAdvertisers = getBlockedAdvertisers(hrh);
        double minimumRtbFloor = 0.05;
        casInternalRequestParametersGlobal.auctionBidFloor = hrh.responseSender.getAuctionEngine().calculateAuctionFloor(
                hrh.responseSender.sasParams.getSiteFloor(), 0.0, segmentFloor, minimumRtbFloor, networkSiteEcpm);
        casInternalRequestParametersGlobal.auctionId = asyncRequestMaker.getImpressionId(siteIncId);
        LOG.debug("RTB floor from the pricing engine entity is {}", rtbdFloor);
        LOG.debug("RTB floor from the pricing engine entity is {}", segmentFloor);
        LOG.debug("Highest Ecpm is {}", casInternalRequestParametersGlobal.highestEcpm);
        LOG.debug("BlockedCategories are {}", casInternalRequestParametersGlobal.blockedCategories);
        LOG.debug("BlockedAdvertisers are {}", casInternalRequestParametersGlobal.blockedAdvertisers);
        LOG.debug("Site floor is {}", siteFloor);
        LOG.debug("NetworkSiteEcpm is {}", networkSiteEcpm);
        LOG.debug("SegmentFloor is {}", segmentFloor);
        LOG.debug("Minimum rtb floor is {}", minimumRtbFloor);
        LOG.debug("Final rtbFloor is {}", casInternalRequestParametersGlobal.auctionBidFloor);
        LOG.debug("Auction id generated is {}", casInternalRequestParametersGlobal.auctionId);
    }

    private double getHighestEcpm(final List<ChannelSegment> channelSegments) {
        double highestEcpm = 0;
        for (ChannelSegment channelSegment : channelSegments) {
            if (channelSegment.getChannelSegmentFeedbackEntity().getECPM() < 10.0
                    && highestEcpm < channelSegment.getChannelSegmentFeedbackEntity().getECPM()) {
                highestEcpm = channelSegment.getChannelSegmentFeedbackEntity().getECPM();
            }
        }
        return highestEcpm;
    }

    private List<Long> getBlockedCategories(final HttpRequestHandler hrh) {
        List<Long> blockedCategories = null;
        if (null != hrh.responseSender.sasParams.getSiteId()) {
            PublisherFilterEntity publisherFilterEntity = CasConfigUtil.repositoryHelper
                    .queryPublisherFilterRepository(hrh.responseSender.sasParams.getSiteId(), 4);
            if (null != publisherFilterEntity && publisherFilterEntity.getBlockedCategories() != null) {
                blockedCategories = Arrays.asList(publisherFilterEntity.getBlockedCategories());
            }
        }
        return blockedCategories;
    }

    private List<String> getBlockedAdvertisers(final HttpRequestHandler hrh) {
        List<String> blockedAdvertisers = null;
        if (null != hrh.responseSender.sasParams.getSiteId()) {
            PublisherFilterEntity publisherFilterEntity = CasConfigUtil.repositoryHelper
                    .queryPublisherFilterRepository(hrh.responseSender.sasParams.getSiteId(), 6);
            if (null != publisherFilterEntity && publisherFilterEntity.getBlockedAdvertisers() != null) {
                blockedAdvertisers = Arrays.asList(publisherFilterEntity.getBlockedAdvertisers());
            }
        }
        return blockedAdvertisers;
    }

	@Override
	public String getName() {
		return null;
	}
	
	protected abstract Logger getLogger();

}
