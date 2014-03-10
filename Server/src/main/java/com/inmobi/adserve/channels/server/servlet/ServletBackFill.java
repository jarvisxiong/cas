package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
import com.inmobi.adserve.channels.server.beans.CasRequest;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
@Path("/backfill")
public class ServletBackFill implements Servlet {
    private static final Logger               LOG = LoggerFactory.getLogger(ServletBackFill.class);

    private final MatchSegments               matchSegments;
    private final Provider<Marker>            traceMarkerProvider;
    private final RequestParser               requestParser;
    private final ChannelSegmentFilterApplier channelSegmentFilterApplier;
    private final CasUtils                    casUtils;
    private final AsyncRequestMaker           asyncRequestMaker;

    @Inject
    ServletBackFill(final MatchSegments matchSegments, final Provider<Marker> traceMarkerProvider,
            final ChannelSegmentFilterApplier channelSegmentFilterApplier, final RequestParser requestParser,
            final CasUtils casUtils, final AsyncRequestMaker asyncRequestMaker) {
        this.matchSegments = matchSegments;
        this.traceMarkerProvider = traceMarkerProvider;
        this.requestParser = requestParser;
        this.channelSegmentFilterApplier = channelSegmentFilterApplier;
        this.casUtils = casUtils;
        this.asyncRequestMaker = asyncRequestMaker;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final CasRequest casRequest) throws Exception {

        Marker traceMarker = traceMarkerProvider.get();

        CasContext casContext = new CasContext();

        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);

        Map<String, List<String>> params = casRequest.queryStringDecoder().parameters();

        Channel serverChannel = casRequest.serverChannel();

        try {
            hrh.jObject = requestParser.extractParams(params);
        }
        catch (JSONException exeption) {
            hrh.jObject = new JSONObject();
            LOG.debug(traceMarker, "Encountered Json Error while creating json object inside HttpRequest Handler");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
        }
        CasInternalRequestParameters casInternalRequestParametersGlobal = new CasInternalRequestParameters();
        SASRequestParameters sasParams = new SASRequestParameters();
        requestParser.parseRequestParameters(hrh.jObject, sasParams, casInternalRequestParametersGlobal);
        hrh.responseSender.sasParams = sasParams;
        LOG.debug(traceMarker, "site floor is  {}", sasParams.getSiteFloor());

        // Increment re Request if request came from rule engine
        if (6 == sasParams.getDst()) {
            LOG.debug(traceMarker, "Request came from rule engin...");
            InspectorStats.incrementStatCount(InspectorStrings.ruleEngineRequests);
        }

        // Send noad if new-category is not present in the request
        if (null == hrh.responseSender.sasParams.getCategories()) {
            LOG.error(traceMarker, "new-category field is not present in the request so sending noad");
            hrh.responseSender.sasParams.setCategories(new ArrayList<Long>());
            hrh.setTerminationReason(ServletHandler.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(InspectorStrings.missingCategory, InspectorStrings.count);
            hrh.responseSender.sendNoAdResponse(serverChannel);
        }

        hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;

        if (null == hrh.responseSender.sasParams) {
            LOG.error(traceMarker, "Terminating request as sasParam is null");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }
        if (null == hrh.responseSender.sasParams.getSiteId()) {
            LOG.error(traceMarker, "Terminating request as site id was missing");
            hrh.setTerminationReason(ServletHandler.missingSiteId);
            InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }
        if (!hrh.responseSender.sasParams.getAllowBannerAds() || hrh.responseSender.sasParams.getSiteFloor() > 5) {
            LOG.error(traceMarker,
                    "Request not being served because of banner not allowed or site floor above threshold");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }
        if (hrh.responseSender.sasParams.getSiteType() != null
                && !ServletHandler.allowedSiteTypes.contains(hrh.responseSender.sasParams.getSiteType())) {
            LOG.error(traceMarker, "Terminating request as incompatible content type");
            hrh.setTerminationReason(ServletHandler.incompatibleSiteType);
            InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }
        if (hrh.responseSender.sasParams.getSdkVersion() != null) {
            try {
                if ((hrh.responseSender.sasParams.getSdkVersion().substring(0, 1).equalsIgnoreCase("i") || hrh.responseSender.sasParams
                        .getSdkVersion().substring(0, 1).equalsIgnoreCase("a"))
                        && Integer.parseInt(hrh.responseSender.sasParams.getSdkVersion().substring(1, 2)) < 3) {
                    LOG.error(traceMarker, "Terminating request as sdkVersion is less than 3");
                    hrh.setTerminationReason(ServletHandler.lowSdkVersion);
                    InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
                    hrh.responseSender.sendNoAdResponse(serverChannel);
                    return;
                }
                else {
                    LOG.debug(traceMarker, "sdk-version : {}", hrh.responseSender.sasParams.getSdkVersion());
                }
            }
            catch (StringIndexOutOfBoundsException exception) {
                LOG.error(traceMarker, "Invalid sdk-version {}", exception);
            }
            catch (NumberFormatException exception) {
                LOG.error(traceMarker, "Invalid sdk-version {}", exception);
            }

        }

        if (ServletHandler.random.nextInt(100) >= ServletHandler.percentRollout) {
            LOG.debug(traceMarker, "Request not being served because of limited percentage rollout");
            InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
            hrh.responseSender.sendNoAdResponse(serverChannel);
        }

        /**
         * Set imai content if r-format is imai
         */
        String imaiBaseUrl = null;
        String rFormat = hrh.responseSender.getResponseFormat();
        if (rFormat.equalsIgnoreCase("imai")) {
            if (hrh.responseSender.sasParams.getPlatformOsId() == 3) {
                imaiBaseUrl = ServletHandler.getServerConfig().getString("androidBaseUrl");
            }
            else {
                imaiBaseUrl = ServletHandler.getServerConfig().getString("iPhoneBaseUrl");
            }
        }
        hrh.responseSender.sasParams.setImaiBaseUrl(imaiBaseUrl);
        LOG.debug(traceMarker, "imai base url is {}", hrh.responseSender.sasParams.getImaiBaseUrl());

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

        String advertisers;
        String[] advertiserList = null;
        try {
            JSONObject uObject = (JSONObject) hrh.jObject.get("uparams");
            if (uObject.get("u-adapter") != null) {
                advertisers = (String) uObject.get("u-adapter");
                advertiserList = advertisers.split(",");
            }
        }
        catch (JSONException exception) {
            LOG.debug(traceMarker, "Some thing went wrong in finding adapters for end to end testing");
        }

        Set<String> advertiserSet = new HashSet<String>();

        if (advertiserList != null) {
            Collections.addAll(advertiserSet, advertiserList);
        }

        casInternalRequestParametersGlobal.highestEcpm = getHighestEcpm(filteredSegments);
        LOG.debug(traceMarker, "Highest Ecpm is {}", casInternalRequestParametersGlobal.highestEcpm);
        casInternalRequestParametersGlobal.blockedCategories = getBlockedCategories(hrh);
        casInternalRequestParametersGlobal.blockedAdvertisers = getBlockedAdvertisers(hrh);
        LOG.debug(traceMarker, "blockedCategories are {}", casInternalRequestParametersGlobal.blockedCategories);
        LOG.debug(traceMarker, "blockedAdvertisers are {}", casInternalRequestParametersGlobal.blockedAdvertisers);
        double minimumRtbFloor = 0.05;
        double segmentFloor = casUtils.getRtbFloor(casContext, sasParams);
        // RTB floor is being passed as segmentFloor
        LOG.debug(traceMarker, "RTB floor from the pricing engine entity is {}", segmentFloor);

        casInternalRequestParametersGlobal.rtbBidFloor = hrh.responseSender.getAuctionEngine().calculateRTBFloor(
                sasParams.getSiteFloor(), 0.0, segmentFloor, minimumRtbFloor);
        LOG.debug(traceMarker, "site floor was {}  segmentFloor was  {}  minimum rtb floor {}  and rtbFloor is {} ",
                sasParams.getSiteFloor(), segmentFloor, minimumRtbFloor, casInternalRequestParametersGlobal.rtbBidFloor);
        // Generating auction id using site Inc Id
        casInternalRequestParametersGlobal.auctionId = asyncRequestMaker.getImpressionId(sasParams.getSiteIncId());
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

        LOG.debug(traceMarker, "Total channels available for sending requests {}", filteredSegments.size());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
        List<ChannelSegment> dcpSegments;

        dcpSegments = asyncRequestMaker.prepareForAsyncRequest(filteredSegments, ServletHandler.getServerConfig(),
                ServletHandler.getRtbConfig(), ServletHandler.getAdapterConfig(), hrh.responseSender, advertiserSet,
                serverChannel, ServletHandler.repositoryHelper, hrh.jObject, hrh.responseSender.sasParams,
                casInternalRequestParametersGlobal, rtbSegments);

        LOG.debug(traceMarker, "rtb rankList size is {}", rtbSegments.size());
        if (dcpSegments.isEmpty() && rtbSegments.isEmpty()) {
            LOG.debug(traceMarker, "No successful configuration of adapter ");
            hrh.responseSender.sendNoAdResponse(serverChannel);
            return;
        }

        List<ChannelSegment> tempRankList = asyncRequestMaker.makeAsyncRequests(dcpSegments, serverChannel, rtbSegments);

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

    private static double getHighestEcpm(final List<ChannelSegment> channelSegments) {
        double highestEcpm = 0;
        for (ChannelSegment channelSegment : channelSegments) {
            if (channelSegment.getChannelSegmentFeedbackEntity().getECPM() < 10.0
                    && highestEcpm < channelSegment.getChannelSegmentFeedbackEntity().getECPM()) {
                highestEcpm = channelSegment.getChannelSegmentFeedbackEntity().getECPM();
            }
        }
        return highestEcpm;
    }

    private static List<Long> getBlockedCategories(final HttpRequestHandler hrh) {
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

    private static List<String> getBlockedAdvertisers(final HttpRequestHandler hrh) {
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
