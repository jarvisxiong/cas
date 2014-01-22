package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PublisherFilterEntity;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.*;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class ServletBackFill implements Servlet {
    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws Exception {

        CasInternalRequestParameters casInternalRequestParametersGlobal = new CasInternalRequestParameters();
        SASRequestParameters sasParams = hrh.responseSender.sasParams;
        if (null == sasParams) {
            sasParams = new SASRequestParameters();
        }

        Map<String, List<String>> params = queryStringDecoder.getParameters();
        // Handling GET requests
        if (params.containsKey("args")) {
            if (null == hrh.jObject) {
                InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
                try {
                    hrh.jObject = RequestParser.extractParams(params);
                }
                catch (JSONException exception) {
                    hrh.jObject = new JSONObject();
                    logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
                    hrh.setTerminationReason(ServletHandler.jsonParsingError);
                    InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
                }
                RequestParser
                        .parseRequestParameters(hrh.jObject, sasParams, casInternalRequestParametersGlobal, logger);
                sasParams.setDst(2);
            }
        } // Handling post requests
        else if (null == hrh.tObject) {
            hrh.tObject = ThriftRequestParser.extractParams(params, logger);
            ThriftRequestParser.parseRequestParameters(hrh.tObject, sasParams, casInternalRequestParametersGlobal,
                logger, 2);
        }
        logger.debug("sasparams in backfill are: " + sasParams);
        hrh.responseSender.sasParams = sasParams;
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;

        if (RequestFilters.isDroppedInRequestFilters(hrh, logger)) {
            logger.debug("Request is dropped in request filters");
            hrh.responseSender.sendNoAdResponse(e);
            return;
        }

        hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;

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
        logger.debug("imai base url is", hrh.responseSender.sasParams.getImaiBaseUrl());

        // getting the selected third party site details
        Map<String, HashMap<String, ChannelSegment>> matchedSegments = new MatchSegments(
                ServletHandler.repositoryHelper, hrh.responseSender.sasParams, logger)
                .matchSegments(hrh.responseSender.sasParams);

        if (matchedSegments == null || matchedSegments.isEmpty()) {
            logger.debug("No Entities matching the request.");
            hrh.responseSender.sendNoAdResponse(e);
            return;
        }

        hrh.responseSender.sasParams.setSiteFloor(0.0);
        Filters filter = new Filters(matchedSegments, ServletHandler.getServerConfig(),
                ServletHandler.getAdapterConfig(), hrh.responseSender.sasParams, ServletHandler.repositoryHelper,
                logger);
        // applying all the filters
        List<ChannelSegment> filteredSegments = filter.applyFilters();

        if (filteredSegments == null || filteredSegments.size() == 0) {
            logger.debug("All segments dropped in filters");
            hrh.responseSender.sendNoAdResponse(e);
            return;
        }

        enrichCasInternalRequestParameters(logger, hrh, filteredSegments,
            casInternalRequestParametersGlobal.rtbBidFloor, sasParams.getSiteFloor(), sasParams.getSiteIncId());
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

        logger.debug("Total channels available for sending requests " + filteredSegments.size());
        List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
        List<ChannelSegment> dcpSegments;

        dcpSegments = AsyncRequestMaker.prepareForAsyncRequest(filteredSegments, logger,
            ServletHandler.getServerConfig(), ServletHandler.getRtbConfig(), ServletHandler.getAdapterConfig(),
            hrh.responseSender, sasParams.getUAdapters(), e, ServletHandler.repositoryHelper, hrh.jObject,
            hrh.responseSender.sasParams, casInternalRequestParametersGlobal, rtbSegments);

        logger.debug("rtb rankList size is", rtbSegments.size());
        if (dcpSegments.isEmpty() && rtbSegments.isEmpty()) {
            logger.debug("No successful configuration of adapter ");
            hrh.responseSender.sendNoAdResponse(e);
            return;
        }

        List<ChannelSegment> tempRankList;

        boolean gDFlag = ServletHandler.getServerConfig().getBoolean("guaranteedDelivery");
        if (gDFlag) {
            tempRankList = filter.rankAdapters(dcpSegments);
            if (!tempRankList.isEmpty()) {
                tempRankList = filter.ensureGuaranteedDelivery(tempRankList);
            }
            if (!rtbSegments.isEmpty()) {
                rtbSegments = filter.ensureGuaranteedDeliveryInCaseOfRTB(rtbSegments, tempRankList);
            }
        }
        else {
            tempRankList = dcpSegments;
        }
        tempRankList = AsyncRequestMaker.makeAsyncRequests(tempRankList, logger, e, rtbSegments);

        hrh.responseSender.setRankList(tempRankList);
        hrh.responseSender.getAuctionEngine().setRtbSegments(rtbSegments);
        logger.debug("Number of tpans whose request was successfully completed", hrh.responseSender
                .getRankList()
                    .size());
        logger.debug("Number of rtb tpans whose request was successfully completed", hrh.responseSender
                .getAuctionEngine()
                    .getRtbSegments()
                    .size());
        // if none of the async request succeed, we return "NO_AD"
        if (hrh.responseSender.getRankList().isEmpty()
                && hrh.responseSender.getAuctionEngine().getRtbSegments().isEmpty()) {
            logger.debug("No calls");
            hrh.responseSender.sendNoAdResponse(e);
            return;
        }

        if (hrh.responseSender.getAuctionEngine().isAllRtbComplete()) {
            AdNetworkInterface highestBid = hrh.responseSender.getAuctionEngine().runRtbSecondPriceAuctionEngine();
            if (null != highestBid) {
                logger.debug("Sending rtb response of", highestBid.getName());
                hrh.responseSender.sendAdResponse(highestBid, e);
                // highestBid.impressionCallback();
                return;
            }
            // Resetting the rankIndexToProcess for already completed adapters.
            hrh.responseSender.processDcpList(e);
            logger.debug("returned from send Response, ranklist size is", hrh.responseSender.getRankList().size());
        }
    }

    @Override
    public String getName() {
        return "BackFill";
    }

    private static void enrichCasInternalRequestParameters(DebugLogger logger, HttpRequestHandler hrh,
            List<ChannelSegment> filteredSegments, Double rtbdFloor, Double siteFloor, long siteIncId) {
        CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;
        casInternalRequestParametersGlobal.highestEcpm = getHighestEcpm(filteredSegments);
        casInternalRequestParametersGlobal.blockedCategories = getBlockedCategories(hrh);
        casInternalRequestParametersGlobal.blockedAdvertisers = getBlockedAdvertisers(hrh);
        double minimumRtbFloor = 0.05;
        double segmentFloor = 0.0;
        // RTB floor is being passed as segmentFloor
        if (null != rtbdFloor) {
            segmentFloor = rtbdFloor;
        }
        casInternalRequestParametersGlobal.rtbBidFloor = hrh.responseSender.getAuctionEngine().calculateRTBFloor(
            siteFloor, 0.0, segmentFloor, minimumRtbFloor);
        casInternalRequestParametersGlobal.auctionId = AsyncRequestMaker.getImpressionId(siteIncId);
        logger.debug("RTB floor from the pricing engine entity is", rtbdFloor);
        logger.debug("Highest Ecpm is", casInternalRequestParametersGlobal.highestEcpm);
        logger.debug("BlockedCategories are", casInternalRequestParametersGlobal.blockedCategories);
        logger.debug("BlockedAdvertisers are", casInternalRequestParametersGlobal.blockedAdvertisers);
        logger.debug("Site floor is", siteFloor);
        logger.debug("SegmentFloor is", segmentFloor);
        logger.debug("Minimum rtb floor is", minimumRtbFloor);
        logger.debug("Final rtbFloor is", casInternalRequestParametersGlobal.rtbBidFloor);
        logger.debug("Auction id generated is", casInternalRequestParametersGlobal.auctionId);
    }

    private static double getHighestEcpm(List<ChannelSegment> channelSegments) {
        double highestEcpm = 0;
        for (ChannelSegment channelSegment : channelSegments) {
            if (channelSegment.getChannelSegmentFeedbackEntity().getECPM() < 10.0
                    && highestEcpm < channelSegment.getChannelSegmentFeedbackEntity().getECPM()) {
                highestEcpm = channelSegment.getChannelSegmentFeedbackEntity().getECPM();
            }
        }
        return highestEcpm;
    }

    private static List<Long> getBlockedCategories(HttpRequestHandler hrh) {
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

    private static List<String> getBlockedAdvertisers(HttpRequestHandler hrh) {
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
