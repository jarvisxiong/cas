package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PublisherFilterEntity;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Singleton
@Path("/backfill")
public class ServletBackFill implements Servlet {
    private static final Logger               LOG = LoggerFactory.getLogger(ServletBackFill.class);

    private final MatchSegments               matchSegments;
    private final Provider<Marker>            traceMarkerProvider;
    private final ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private final CasUtils                    casUtils;
    private final RequestFilters              requestFilters;
    private final AsyncRequestMaker           asyncRequestMaker;

    @Inject
    ServletBackFill(final MatchSegments matchSegments, final Provider<Marker> traceMarkerProvider,
            final ChannelSegmentFilterApplier channelSegmentFilterApplier, final CasUtils casUtils,
            final RequestFilters requestFilters, final AsyncRequestMaker asyncRequestMaker) {
        this.matchSegments = matchSegments;
        this.traceMarkerProvider = traceMarkerProvider;
        this.channelSegmentFilterApplier = channelSegmentFilterApplier;
        this.casUtils = casUtils;
        this.requestFilters = requestFilters;
        this.asyncRequestMaker = asyncRequestMaker;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        CasContext casContext = new CasContext();
        Marker traceMarker = traceMarkerProvider.get();
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        SASRequestParameters sasParams = hrh.responseSender.sasParams;
        hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;

        if (requestFilters.isDroppedInRequestFilters(hrh)) {
            LOG.debug("Request is dropped in request filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        // Setting isResponseOnlyFromDCP from config
        boolean isResponseOnlyFromDcp = ServletHandler.getServerConfig().getBoolean("isResponseOnyFromDCP", false);
        LOG.debug("isResponseOnlyFromDcp from config is {}", isResponseOnlyFromDcp);
        sasParams.setResponseOnlyFromDcp(isResponseOnlyFromDcp);

        // Set imai content if r-format is imai
        String imaiBaseUrl = null;
        String rFormat = hrh.responseSender.getResponseFormat();
        if (rFormat.equalsIgnoreCase("imai")) {
            if (hrh.responseSender.sasParams.getOsId() == 3) {
                imaiBaseUrl = ServletHandler.getServerConfig().getString("androidBaseUrl");
            }
            else {
                imaiBaseUrl = ServletHandler.getServerConfig().getString("iPhoneBaseUrl");
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

        hrh.responseSender.sasParams.setSiteFloor(0.0);

        // applying all the filters
        List<ChannelSegment> filteredSegments = channelSegmentFilterApplier.getChannelSegments(matchedSegmentDetails,
                sasParams, casContext);

        if (filteredSegments == null || filteredSegments.size() == 0) {
            LOG.debug(traceMarker, "All segments dropped in filters");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        double networkSiteEcpm = casUtils.getNetworkSiteEcpm(casContext, sasParams);
        double segmentFloor = casUtils.getRtbFloor(casContext, hrh.responseSender.sasParams);
        enrichCasInternalRequestParameters(hrh, filteredSegments, casInternalRequestParametersGlobal.rtbBidFloor,
                sasParams.getSiteFloor(), sasParams.getSiteIncId(), networkSiteEcpm, segmentFloor);
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

        LOG.debug("Total channels available for sending requests {}", filteredSegments.size());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
        List<ChannelSegment> dcpSegments;

        dcpSegments = asyncRequestMaker.prepareForAsyncRequest(filteredSegments, ServletHandler.getServerConfig(),
                ServletHandler.getRtbConfig(), ServletHandler.getAdapterConfig(), hrh.responseSender,
                sasParams.getUAdapters(), serverChannel, ServletHandler.repositoryHelper, hrh.jObject,
                hrh.responseSender.sasParams, casInternalRequestParametersGlobal, rtbSegments);

        LOG.debug("rtb rankList size is {}", rtbSegments.size());
        if (dcpSegments.isEmpty() && rtbSegments.isEmpty()) {
            LOG.debug("No successful configuration of adapter ");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        List<ChannelSegment> tempRankList = asyncRequestMaker
                .makeAsyncRequests(dcpSegments, serverChannel, rtbSegments);

        hrh.responseSender.setRankList(tempRankList);
        hrh.responseSender.getAuctionEngine().setRtbSegments(rtbSegments);
        LOG.debug(traceMarker, "Number of tpans whose request was successfully completed {}", hrh.responseSender
                .getRankList().size());
        LOG.debug(traceMarker, "Number of rtb tpans whose request was successfully completed {}", hrh.responseSender
                .getAuctionEngine().getRtbSegments().size());
        // if none of the async request succeed, we return "NO_AD"
        if (hrh.responseSender.getRankList().isEmpty()
                && hrh.responseSender.getAuctionEngine().getRtbSegments().isEmpty()) {
            LOG.debug(traceMarker, "No calls");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        if (hrh.responseSender.getAuctionEngine().isAllRtbComplete()) {
            AdNetworkInterface highestBid = hrh.responseSender.getAuctionEngine().runRtbSecondPriceAuctionEngine();
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

    @Override
    public String getName() {
        return "BackFill";
    }

    private void enrichCasInternalRequestParameters(final HttpRequestHandler hrh,
            final List<ChannelSegment> filteredSegments, final Double rtbdFloor, final Double siteFloor,
            final long siteIncId, final double networkSiteEcpm, final double segmentFloor) {
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;
        casInternalRequestParametersGlobal.highestEcpm = getHighestEcpm(filteredSegments);
        casInternalRequestParametersGlobal.blockedCategories = getBlockedCategories(hrh);
        casInternalRequestParametersGlobal.blockedAdvertisers = getBlockedAdvertisers(hrh);
        double minimumRtbFloor = 0.05;
        casInternalRequestParametersGlobal.rtbBidFloor = hrh.responseSender.getAuctionEngine().calculateRTBFloor(
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
        LOG.debug("Final rtbFloor is {}", casInternalRequestParametersGlobal.rtbBidFloor);
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
            PublisherFilterEntity publisherFilterEntity = ServletHandler.repositoryHelper
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
            PublisherFilterEntity publisherFilterEntity = ServletHandler.repositoryHelper
                    .queryPublisherFilterRepository(hrh.responseSender.sasParams.getSiteId(), 6);
            if (null != publisherFilterEntity && publisherFilterEntity.getBlockedAdvertisers() != null) {
                blockedAdvertisers = Arrays.asList(publisherFilterEntity.getBlockedAdvertisers());
            }
        }
        return blockedAdvertisers;
    }

}
