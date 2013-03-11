package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
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
    hrh.responseSender.sasParams = RequestParser.parseRequestParameters(hrh.jObject, logger);
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
      logger.error("Terminating request as incompatible content type");
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
          logger.error("Terminating request as sdkVersion is less than 3");
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
     * if sendonlytowhitelist flag is true, check if site id is present in
     * whitelist, else send no ad.
     */
    if(ServletHandler.config.getBoolean("sendOnlyToWhitelist")) {
      List<String> whitelist = ServletHandler.config.getList("whitelist");
      if(null == whitelist || !whitelist.contains(hrh.responseSender.sasParams.getSiteId())) {
        logger.debug("site id not present in whitelist, so sending no ad response");
        hrh.responseSender.sendNoAdResponse(e);
        return;
      }
    }

    /**
     * Set imai content if r-format is imai
     */
    String imaiBaseUrl = null;
    String rFormat = hrh.responseSender.getResponseFormat();
    if(rFormat.equalsIgnoreCase("imai")) {
      if(hrh.responseSender.sasParams.getPlatformOsId() == 3) {
        imaiBaseUrl = ServletHandler.config.getString("androidBaseUrl");
      } else {
        imaiBaseUrl = ServletHandler.config.getString("iPhoneBaseUrl");
      }
    }
    hrh.responseSender.sasParams.setImaiBaseUrl(imaiBaseUrl);
    logger.debug("imai base url is", hrh.responseSender.sasParams.getImaiBaseUrl());
    
    //Send noad if new-category is not present in the request
    if (null == hrh.responseSender.sasParams.getCategories()) {
      hrh.logger.debug("new-category field is not present in the request so sending noad");
      hrh.responseSender.sendNoAdResponse(e);
    }
    // getting the selected third party site details
    Map<String, HashMap<String, ChannelSegment>> matchedSegments = new MatchSegments(ServletHandler.repositoryHelper,
        hrh.responseSender.sasParams, logger).matchSegments(hrh.responseSender.sasParams);

    if(matchedSegments == null) {
      logger.debug("No Entities matching the request.");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    hrh.responseSender.sasParams.setSiteFloor(0.0);
    Filters filter = new Filters(matchedSegments, ServletHandler.config, ServletHandler.adapterConfig,
        hrh.responseSender.sasParams, ServletHandler.repositoryHelper, logger);
    // applying all the filters
    List<ChannelSegment> rows = filter.applyFilters();

    if(rows == null || rows.size() == 0) {
      hrh.responseSender.sendNoAdResponse(e);
      logger.debug("All segments dropped in filters");
      return;
    }

    List<ChannelSegment> segments;

    String advertisers = "";
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
      for (int i = 0; i < advertiserList.length; i++) {
        advertiserSet.add(advertiserList[i]);
      }
    }

    CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    casInternalRequestParameters.highestEcpm = getHighestEcpm(rows, logger);
    logger.debug("Highest Ecpm is", Double.valueOf(casInternalRequestParameters.highestEcpm));
    casInternalRequestParameters.blockedCategories = getBlockedCategories(hrh, logger);
    casInternalRequestParameters.rtbBidFloor = hrh.responseSender.sasParams.getSiteFloor() > casInternalRequestParameters.highestEcpm ? hrh.responseSender.sasParams.getSiteFloor()
        : casInternalRequestParameters.highestEcpm + 0.01;
    hrh.responseSender.casInternalRequestParameters = casInternalRequestParameters;
    hrh.responseSender.getAuctionEngine().casInternalRequestParameters = casInternalRequestParameters;
    logger.debug("Total channels available for sending requests " + rows.size());
    List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

    segments = AsyncRequestMaker.prepareForAsyncRequest(rows, logger, ServletHandler.config, ServletHandler.rtbConfig,
        ServletHandler.adapterConfig, hrh.responseSender, advertiserSet, e, ServletHandler.repositoryHelper,
        hrh.jObject, hrh.responseSender.sasParams, casInternalRequestParameters, rtbSegments);

    logger.debug("rtb rankList size is", Integer.valueOf(rtbSegments.size()));
    if(segments.isEmpty() && rtbSegments.isEmpty()) {
      logger.debug("No succesfull configuration of adapter ");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    List<ChannelSegment> tempRankList;
    tempRankList = filter.rankAdapters(segments);

    if(!tempRankList.isEmpty()) {
      tempRankList = filter.ensureGuaranteedDelivery(tempRankList);
    }
    tempRankList = AsyncRequestMaker.makeAsyncRequests(tempRankList, logger, hrh.responseSender, e, rtbSegments);

    hrh.responseSender.setRankList(tempRankList);
    hrh.responseSender.getAuctionEngine().setRtbSegments(rtbSegments);
    if(logger.isDebugEnabled()) {
      logger.debug("Number of tpans whose request was successfully completed "
          + hrh.responseSender.getRankList().size());
      logger.debug("Number of rtb tpans whose request was successfully completed "
          + hrh.responseSender.getAuctionEngine().getRtbSegments().size());
    }
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
      if(logger.isDebugEnabled()) {
        logger.debug("returned from send Response, ranklist size is " + hrh.responseSender.getRankList().size());
      }
    }
  }

  @Override
  public String getName() {
    return "BackFill";
  }

  private static double getHighestEcpm(List<ChannelSegment> channelSegments, DebugLogger logger) {
    double lowestEcpm = 0;
    for (ChannelSegment channelSegment : channelSegments) {
      if(logger.isDebugEnabled())
      lowestEcpm = lowestEcpm < channelSegment.getChannelSegmentFeedbackEntity().geteCPM() ? channelSegment
          .getChannelSegmentFeedbackEntity().geteCPM() : lowestEcpm;
    }
    return lowestEcpm;
  }

  private static List<Long> getBlockedCategories(HttpRequestHandler hrh, DebugLogger logger) {
    List<Long> blockedCategories = null;
    if(null != hrh.responseSender.sasParams.getSiteId()) {
      logger.debug("SiteId is", hrh.responseSender.sasParams.getSiteId());
      SiteMetaDataEntity siteMetaDataEntity = ServletHandler.repositoryHelper
          .querySiteMetaDetaRepository(hrh.responseSender.sasParams.getSiteId());
      if(null != siteMetaDataEntity && siteMetaDataEntity.getBlockedCategories() != null) {
        if(!siteMetaDataEntity.isExpired() && siteMetaDataEntity.getRuleType() == 4) {
          blockedCategories = Arrays.asList(siteMetaDataEntity.getBlockedCategories());
          logger.debug("Site id is", hrh.responseSender.sasParams.getSiteId(), "no of blocked categories are");
        }
      } else
        logger.debug("No blockedCategory for this site id");
    }
    return blockedCategories;
  }
}
