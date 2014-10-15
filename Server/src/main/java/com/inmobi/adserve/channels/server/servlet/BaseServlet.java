package com.inmobi.adserve.channels.server.servlet;

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
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.DemandSourceType;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        SASRequestParameters sasParams = hrh.responseSender.sasParams;

        hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;

        casInternalRequestParametersGlobal.setTraceEnabled(Boolean.valueOf(hrh.getHttpRequest().headers().get("x-mkhoj-tracer")));

        if (requestFilters.isDroppedInRequestFilters(hrh)) {
            LOG.debug("Request is dropped in request filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        // Setting isResponseOnlyFromDCP from config
        boolean isResponseOnlyFromDcp = CasConfigUtil.getServerConfig().getBoolean("isResponseOnyFromDCP", false);
        LOG.debug("isResponseOnlyFromDcp from config is {}", isResponseOnlyFromDcp);
        sasParams.setResponseOnlyFromDcp(isResponseOnlyFromDcp);

        // Setting isBannerVideoSupported based on the Request params. Only supported for RTBD request.
        if (DemandSourceType.RTBD == DemandSourceType.findByValue(sasParams.getDst())) {
            boolean isBannerVideoSupported = casUtils.isBannerVideoSupported(sasParams);
            LOG.debug("isBannerVideoSupported for this request is {}", isBannerVideoSupported);
            sasParams.setBannerVideoSupported(isBannerVideoSupported);
        }

        // Set imai content if r-format is imai
        String imaiBaseUrl = null;
        if (hrh.responseSender.getResponseFormat() == ResponseFormat.IMAI) {
            if (hrh.responseSender.sasParams.getOsId() == 3) {
                imaiBaseUrl = CasConfigUtil.getServerConfig().getString("androidBaseUrl");
            } else {
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
        casInternalRequestParametersGlobal.setSiteAccountType(AccountType.SELF_SERVE);
        if (null != siteMetaDataEntity) {
            casInternalRequestParametersGlobal.setSiteAccountType(siteMetaDataEntity.getAccountTypesAllowed());
        }

        // applying all the filters
        List<ChannelSegment> filteredSegments = channelSegmentFilterApplier.getChannelSegments(matchedSegmentDetails,
                sasParams, casContext, advertiserLevelFilters, adGroupLevelFilters);

        if (filteredSegments == null || filteredSegments.isEmpty()) {
            LOG.debug(traceMarker, "All segments dropped in filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        double networkSiteEcpm = casUtils.getNetworkSiteEcpm(casContext, sasParams);
        double segmentFloor = casUtils.getRtbFloor(casContext, hrh.responseSender.sasParams);
        enrichCasInternalRequestParameters(hrh, filteredSegments, casInternalRequestParametersGlobal.getAuctionBidFloor(),
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
        if (CollectionUtils.isEmpty(dcpSegments) && CollectionUtils.isEmpty(rtbSegments)) {
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
            LOG.debug(traceMarker, "returned from send Response, ranklist size is {}",  hrh.responseSender.getRankList().size());
        }
    }

    private void enrichCasInternalRequestParameters(final HttpRequestHandler hrh,
            final List<ChannelSegment> filteredSegments, final Double rtbdFloor, final Double siteFloor,
            final long siteIncId, final double networkSiteEcpm, final double segmentFloor) {
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;
        casInternalRequestParametersGlobal.setHighestEcpm(getHighestEcpm(filteredSegments));
        casInternalRequestParametersGlobal.setBlockedIabCategories(getBlockedIabCategories(hrh));
        casInternalRequestParametersGlobal.setBlockedAdvertisers(getBlockedAdvertisers(hrh));
        double minimumRtbFloor = 0.05;
        casInternalRequestParametersGlobal.setAuctionBidFloor(hrh.responseSender.getAuctionEngine().calculateAuctionFloor(
                hrh.responseSender.sasParams.getSiteFloor(), 0.0, segmentFloor, minimumRtbFloor, networkSiteEcpm));
        casInternalRequestParametersGlobal.setAuctionId(ImpressionIdGenerator.getInstance().getImpressionId(siteIncId));
        LOG.debug("RTB floor from the pricing engine entity is {}", rtbdFloor);
        LOG.debug("RTB floor from the pricing engine entity is {}", segmentFloor);
        LOG.debug("Highest Ecpm is {}", casInternalRequestParametersGlobal.getHighestEcpm());
        LOG.debug("BlockedCategories are {}", casInternalRequestParametersGlobal.getBlockedIabCategories());
        LOG.debug("BlockedAdvertisers are {}", casInternalRequestParametersGlobal.getBlockedAdvertisers());
        LOG.debug("Site floor is {}", siteFloor);
        LOG.debug("NetworkSiteEcpm is {}", networkSiteEcpm);
        LOG.debug("SegmentFloor is {}", segmentFloor);
        LOG.debug("Minimum rtb floor is {}", minimumRtbFloor);
        LOG.debug("Final rtbFloor is {}", casInternalRequestParametersGlobal.getAuctionBidFloor());
        LOG.debug("Auction id generated is {}", casInternalRequestParametersGlobal.getAuctionId());
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

    private List<String> getBlockedIabCategories(final HttpRequestHandler hrh) {
        List<String> blockedCategories = null;
        if (null != hrh.responseSender.sasParams.getSiteId()) {
            SiteFilterEntity siteFilterEntity = CasConfigUtil.repositoryHelper
                    .querySiteFilterRepository(hrh.responseSender.sasParams.getSiteId(), 4);
            if (null != siteFilterEntity && siteFilterEntity.getBlockedIabCategories() != null) {
                blockedCategories = Arrays.asList(siteFilterEntity.getBlockedIabCategories());
            }
        }
        return blockedCategories;
    }

    private List<String> getBlockedAdvertisers(final HttpRequestHandler hrh) {
        List<String> blockedAdvertisers = null;
        if (null != hrh.responseSender.sasParams.getSiteId()) {
            SiteFilterEntity siteFilterEntity = CasConfigUtil.repositoryHelper
                    .querySiteFilterRepository(hrh.responseSender.sasParams.getSiteId(), 6);
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

}
