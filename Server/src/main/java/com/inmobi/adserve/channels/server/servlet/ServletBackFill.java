package com.inmobi.adserve.channels.server.servlet;

import java.util.*;

import com.inmobi.adserve.channels.entity.PublisherFilterEntity;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.ServletHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Filters;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletBackFill implements Servlet {
  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {

    InspectorStats.incrementStatCount(InspectorStrings.totalRequests);

    Map<String, List<String>> params = queryStringDecoder.getParameters();

    try {
      hrh.jObject = RequestParser.extractParams(params, logger);
    } catch (JSONException exeption) {
      hrh.jObject = new JSONObject();
      logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
      hrh.setTerminationReason(ServletHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
    }
    CasInternalRequestParameters casInternalRequestParametersGlobal = new CasInternalRequestParameters();
    SASRequestParameters sasParams = new SASRequestParameters();
    RequestParser.parseRequestParameters(hrh.jObject, sasParams, casInternalRequestParametersGlobal, logger);
    hrh.responseSender.sasParams = sasParams;
    
    //Send noad if new-category is not present in the request
    if (null == hrh.responseSender.sasParams.getCategories()) {
      hrh.logger.debug("new-category field is not present in the request so sending noad");
      hrh.responseSender.sasParams.setCategories(new ArrayList<Long>());
      hrh.setTerminationReason(ServletHandler.MISSING_CATEGORY);
      InspectorStats.incrementStatCount(InspectorStrings.missingCategory, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
    }
    
    hrh.responseSender.getAuctionEngine().sasParams = hrh.responseSender.sasParams;
    if(ServletHandler.random.nextInt(100) >= ServletHandler.percentRollout) {
      logger.debug("Request not being served because of limited percentage rollout");
      InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
    }
    if(null == hrh.responseSender.sasParams) {
      logger.debug("Terminating request as sasParam is null");
      hrh.setTerminationReason(ServletHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(null == hrh.responseSender.sasParams.getSiteId()) {
      logger.debug("Terminating request as site id was missing");
      hrh.setTerminationReason(ServletHandler.missingSiteId);
      InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(!hrh.responseSender.sasParams.getAllowBannerAds() || hrh.responseSender.sasParams.getSiteFloor() > 5) {
      logger.debug("Request not being served because of banner not allowed or site floor above threshold");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(hrh.responseSender.sasParams.getSiteType() != null
            && !ServletHandler.allowedSiteTypes.contains(hrh.responseSender.sasParams.getSiteType())) {
      logger.info("Terminating request as incompatible content type");
      hrh.setTerminationReason(ServletHandler.incompatibleSiteType);
      InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(hrh.responseSender.sasParams.getSdkVersion() != null) {
      try {
        if((hrh.responseSender.sasParams.getSdkVersion().substring(0, 1).equalsIgnoreCase("i") || hrh.responseSender.sasParams.getSdkVersion()
            .substring(0, 1).equalsIgnoreCase("a"))
            && Integer.parseInt(hrh.responseSender.sasParams.getSdkVersion().substring(1, 2)) < 3) {
          logger.debug("Terminating request as sdkVersion is less than 3");
          hrh.setTerminationReason(ServletHandler.lowSdkVersion);
          InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
          hrh.responseSender.sendNoAdResponse(e);
          return;
        } else
          logger.debug("sdk-version : " + hrh.responseSender.sasParams.getSdkVersion());
      } catch (StringIndexOutOfBoundsException e2) {
        logger.debug("Invalid sdkversion " + e2.getMessage());
      } catch (NumberFormatException e3) {
        logger.debug("Invalid sdkversion " + e3.getMessage());
      }

    }

    /**
     * Set imai content if r-format is imai
     */
    String imaiBaseUrl = null;
    String rFormat = hrh.responseSender.getResponseFormat();
    if(rFormat.equalsIgnoreCase("imai")) {
      if(hrh.responseSender.sasParams.getPlatformOsId() == 3) {
        imaiBaseUrl = ServletHandler.getServerConfig().getString("androidBaseUrl");
      } else {
        imaiBaseUrl = ServletHandler.getServerConfig().getString("iPhoneBaseUrl");
      }
    }
    hrh.responseSender.sasParams.setImaiBaseUrl(imaiBaseUrl);
    logger.debug("imai base url is", hrh.responseSender.sasParams.getImaiBaseUrl());
    
    // getting the selected third party site details
    Map<String, HashMap<String, ChannelSegment>> matchedSegments = new MatchSegments(ServletHandler.repositoryHelper,
        hrh.responseSender.sasParams, logger).matchSegments(hrh.responseSender.sasParams);

    if(matchedSegments == null) {
      logger.debug("No Entities matching the request.");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    hrh.responseSender.sasParams.setSiteFloor(0.0);
    Filters filter = new Filters(matchedSegments, ServletHandler.getServerConfig(), ServletHandler.getAdapterConfig(),
        hrh.responseSender.sasParams, ServletHandler.repositoryHelper, logger);
    // applying all the filters
    List<ChannelSegment> filteredSegments = filter.applyFilters();

    if(filteredSegments == null || filteredSegments.size() == 0) {
      hrh.responseSender.sendNoAdResponse(e);
      logger.debug("All segments dropped in filters");
      return;
    }

    String advertisers;
    String[] advertiserList = null;
    try {
      JSONObject uObject = (JSONObject) hrh.jObject.get("uparams");
      if(uObject.get("u-adapter") != null) {
        advertisers = (String) uObject.get("u-adapter");
        advertiserList = advertisers.split(",");
      }
    } catch (JSONException exception) {
      logger.debug("Some thing went wrong in finding adapters for end to end testing");
    }

    Set<String> advertiserSet = new HashSet<String>();

    if(advertiserList != null) {
        Collections.addAll(advertiserSet, advertiserList);
    }

    casInternalRequestParametersGlobal.highestEcpm = getHighestEcpm(filteredSegments);
    logger.debug("Highest Ecpm is", casInternalRequestParametersGlobal.highestEcpm);
    casInternalRequestParametersGlobal.blockedCategories = getBlockedCategories(hrh, logger);
    casInternalRequestParametersGlobal.blockedAdvertisers = getBlockedAdvertisers(hrh, logger);
    logger.debug("blockedCategories are", casInternalRequestParametersGlobal.blockedCategories);
    logger.debug("blockedAdvertisers are", casInternalRequestParametersGlobal.blockedAdvertisers);
    double minimumRtbFloor = 0.05;
    double segmentFloor = 0.0;
    //RTB floor is being passed as segmentFloor
    logger.debug("RTB floor from the pricing engine entity is", filter.getRtbFloor());
    if(null != filter.getRtbFloor()) {
        segmentFloor = filter.getRtbFloor();
    }
    casInternalRequestParametersGlobal.rtbBidFloor = hrh.responseSender.getAuctionEngine().calculateRTBFloor(sasParams.getSiteFloor(), 0.0, segmentFloor, minimumRtbFloor);
    //Generating auction id using site Inc Id
    casInternalRequestParametersGlobal.auctionId = AsyncRequestMaker.getImpressionId(sasParams.getSiteIncId());
    hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
    hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParametersGlobal;

    logger.debug("Total channels available for sending requests " + filteredSegments.size());
    List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
    List<ChannelSegment> dcpSegments;

    dcpSegments = AsyncRequestMaker.prepareForAsyncRequest(filteredSegments, logger, ServletHandler.getServerConfig(), ServletHandler.getRtbConfig(),
      ServletHandler.getAdapterConfig(), hrh.responseSender, advertiserSet, e, ServletHandler.repositoryHelper,
      hrh.jObject, hrh.responseSender.sasParams, casInternalRequestParametersGlobal, rtbSegments);

    logger.debug("rtb rankList size is", rtbSegments.size());
    if(dcpSegments.isEmpty() && rtbSegments.isEmpty()) {
      logger.debug("No successful configuration of adapter ");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    List<ChannelSegment> tempRankList;

    boolean gDFlag = ServletHandler.getServerConfig().getBoolean("guaranteedDelivery");
    if (gDFlag) {
      tempRankList = filter.rankAdapters(dcpSegments);
      if(!tempRankList.isEmpty()) {
        tempRankList = filter.ensureGuaranteedDelivery(tempRankList);
      }
      if(!rtbSegments.isEmpty()) {
        rtbSegments = filter.ensureGuaranteedDeliveryInCaseOfRTB(rtbSegments, tempRankList);
      }
    } else {
      tempRankList = dcpSegments;
    }
    tempRankList = AsyncRequestMaker.makeAsyncRequests(tempRankList, logger, e, rtbSegments);

    hrh.responseSender.setRankList(tempRankList);
    hrh.responseSender.getAuctionEngine().setRtbSegments(rtbSegments);
    logger.debug("Number of tpans whose request was successfully completed", hrh.responseSender.getRankList().size());
    logger.debug("Number of rtb tpans whose request was successfully completed", hrh.responseSender.getAuctionEngine().getRtbSegments().size());
    // if none of the async request succeed, we return "NO_AD"
    if(hrh.responseSender.getRankList().isEmpty() && hrh.responseSender.getAuctionEngine().getRtbSegments().isEmpty()) {
      logger.debug("No calls");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    if(hrh.responseSender.getAuctionEngine().isAllRtbComplete()) {
      AdNetworkInterface highestBid = hrh.responseSender.getAuctionEngine().runRtbSecondPriceAuctionEngine();
      if(null != highestBid) {
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

  private static double getHighestEcpm(List<ChannelSegment> channelSegments) {
    double highestEcpm = 0;
    for (ChannelSegment channelSegment : channelSegments) {
      if(channelSegment.getChannelSegmentFeedbackEntity().getECPM() < 10.0 && 
              highestEcpm < channelSegment.getChannelSegmentFeedbackEntity().getECPM()) {
        highestEcpm = channelSegment.getChannelSegmentFeedbackEntity().getECPM();
      }
    }
    return highestEcpm;
  }

  private static List<Long> getBlockedCategories(HttpRequestHandler hrh, DebugLogger logger) {
    List<Long> blockedCategories = null;
    if(null != hrh.responseSender.sasParams.getSiteId()) {
      PublisherFilterEntity publisherFilterEntity = ServletHandler.repositoryHelper.queryPublisherFilterRepository(
          hrh.responseSender.sasParams.getSiteId(), 4, logger);
      if(null != publisherFilterEntity && publisherFilterEntity.getBlockedCategories() != null) {
        blockedCategories = Arrays.asList(publisherFilterEntity.getBlockedCategories());
      } 
    }
    return blockedCategories;
  }
  
  private static List<String> getBlockedAdvertisers(HttpRequestHandler hrh, DebugLogger logger) {
    List<String> blockedAdvertisers = null;
    if(null != hrh.responseSender.sasParams.getSiteId()) {
      PublisherFilterEntity publisherFilterEntity = ServletHandler.repositoryHelper.queryPublisherFilterRepository(
          hrh.responseSender.sasParams.getSiteId(), 6, logger);
      if(null != publisherFilterEntity && publisherFilterEntity.getBlockedAdvertisers() != null) {
        blockedAdvertisers = Arrays.asList(publisherFilterEntity.getBlockedAdvertisers());
      }
    }
    return blockedAdvertisers;
  }
  
}
