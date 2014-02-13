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
import com.inmobi.adserve.channels.server.requesthandler.*;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.*;


@Singleton
@Path("/backfill")
public class ServletBackFill implements Servlet {
    private static final Logger    LOG = LoggerFactory.getLogger(ServletBackFill.class);

    private final MatchSegments    matchSegments;
    private final Provider<Marker> traceMarkerProvider;
    private final RequestFilters   requestFilters;

    @Inject
    ServletBackFill(final MatchSegments matchSegments, final Provider<Marker> traceMarkerProvider,
                    final RequestFilters   requestFilters) {
        this.matchSegments = matchSegments;
        this.traceMarkerProvider = traceMarkerProvider;
        this.requestFilters = requestFilters;
    }

        @Override
        public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e) throws Exception {

            SASRequestParameters sasParams = hrh.responseSender.sasParams;
            hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;
            CasInternalRequestParameters casInternalRequestParametersGlobal = hrh.responseSender.casInternalRequestParameters;

            if (requestFilters.isDroppedInRequestFilters(hrh)) {
                LOG.debug("Request is dropped in request filters");
                hrh.responseSender.sendNoAdResponse(e);
                return;
            }

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
            Map<String, HashMap<String, ChannelSegment>> matchedSegments = matchSegments
                    .matchSegments(hrh.responseSender.sasParams);

            if (matchedSegments == null || matchedSegments.isEmpty()) {
                LOG.debug("No Entities matching the request.");
                hrh.responseSender.sendNoAdResponse(e);
                return;
            }

            hrh.responseSender.sasParams.setSiteFloor(0.0);
            Filters filter = new Filters(matchedSegments, ServletHandler.getServerConfig(),
                    ServletHandler.getAdapterConfig(), hrh.responseSender.sasParams, ServletHandler.repositoryHelper
                    );
            // applying all the filters
            List<ChannelSegment> filteredSegments = filter.applyFilters();

            if (filteredSegments == null || filteredSegments.size() == 0) {
                LOG.debug("All segments dropped in filters");
                hrh.responseSender.sendNoAdResponse(e);
                return;
            }

            enrichCasInternalRequestParameters(hrh, filteredSegments,
                    casInternalRequestParametersGlobal.rtbBidFloor, sasParams.getSiteFloor(), sasParams.getSiteIncId());
            hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
            hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

            LOG.debug("Total channels available for sending requests {}", filteredSegments.size());
            List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
            List<ChannelSegment> dcpSegments;

            dcpSegments = AsyncRequestMaker.prepareForAsyncRequest(filteredSegments,
                    ServletHandler.getServerConfig(), ServletHandler.getRtbConfig(), ServletHandler.getAdapterConfig(),
                    hrh.responseSender, sasParams.getUAdapters(), e, ServletHandler.repositoryHelper, hrh.jObject,
                    hrh.responseSender.sasParams, casInternalRequestParametersGlobal, rtbSegments);

            LOG.debug("rtb rankList size is {}", rtbSegments.size());
            if (dcpSegments.isEmpty() && rtbSegments.isEmpty()) {
                LOG.debug("No successful configuration of adapter ");
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
            tempRankList = AsyncRequestMaker.makeAsyncRequests(tempRankList, e, rtbSegments);

            hrh.responseSender.setRankList(tempRankList);
            hrh.responseSender.getAuctionEngine().setRtbSegments(rtbSegments);
            LOG.debug("Number of tpans whose request was successfully completed {}", hrh.responseSender
                    .getRankList()
                    .size());
            LOG.debug("Number of rtb tpans whose request was successfully completed {}", hrh.responseSender
                    .getAuctionEngine()
                    .getRtbSegments()
                    .size());
            // if none of the async request succeed, we return "NO_AD"
            if (hrh.responseSender.getRankList().isEmpty()
                    && hrh.responseSender.getAuctionEngine().getRtbSegments().isEmpty()) {
                LOG.debug("No calls");
                hrh.responseSender.sendNoAdResponse(e);
                return;
            }

            if (hrh.responseSender.getAuctionEngine().isAllRtbComplete()) {
                AdNetworkInterface highestBid = hrh.responseSender.getAuctionEngine().runRtbSecondPriceAuctionEngine();
                if (null != highestBid) {
                    LOG.debug("Sending rtb response of {}", highestBid.getName());
                    hrh.responseSender.sendAdResponse(highestBid, e);
                    // highestBid.impressionCallback();
                    return;
                }
                // Resetting the rankIndexToProcess for already completed adapters.
                hrh.responseSender.processDcpList(e);
                LOG.debug("returned from send Response, ranklist size is {}", hrh.responseSender.getRankList().size());
            }
        }

        @Override
        public String getName() {
            return "BackFill";
        }

        private static void enrichCasInternalRequestParameters(HttpRequestHandler hrh,
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
            LOG.debug("RTB floor from the pricing engine entity is {}", rtbdFloor);
            LOG.debug("Highest Ecpm is {}", casInternalRequestParametersGlobal.highestEcpm);
            LOG.debug("BlockedCategories are {}", casInternalRequestParametersGlobal.blockedCategories);
            LOG.debug("BlockedAdvertisers are {}", casInternalRequestParametersGlobal.blockedAdvertisers);
            LOG.debug("Site floor is {}", siteFloor);
            LOG.debug("SegmentFloor is {}", segmentFloor);
            LOG.debug("Minimum rtb floor is {}", minimumRtbFloor);
            LOG.debug("Final rtbFloor is {}", casInternalRequestParametersGlobal.rtbBidFloor);
            LOG.debug("Auction id generated is {}", casInternalRequestParametersGlobal.auctionId);
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