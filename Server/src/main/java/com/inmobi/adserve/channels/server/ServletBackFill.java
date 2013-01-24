package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
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
    if(null == hrh.responseSender.sasParams.siteId) {
      logger.debug("Terminating request as site id was missing");
      hrh.setTerminationReason(ServletHandler.missingSiteId);
      InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(!hrh.responseSender.sasParams.allowBannerAds || hrh.responseSender.sasParams.siteFloor > 5) {
      logger.debug("Request not being served because of banner not allowed or site floor above threshold");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(hrh.responseSender.sasParams.siteType != null
        && !ServletHandler.allowedSiteTypes.contains(hrh.responseSender.sasParams.siteType)) {
      logger.error("Terminating request as incompatible content type");
      hrh.setTerminationReason(ServletHandler.incompatibleSiteType);
      InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }
    if(hrh.responseSender.sasParams.sdkVersion != null) {
      try {
        if((hrh.responseSender.sasParams.sdkVersion.substring(0, 1).equalsIgnoreCase("i") || hrh.responseSender.sasParams.sdkVersion
            .substring(0, 1).equalsIgnoreCase("a"))
            && Integer.parseInt(hrh.responseSender.sasParams.sdkVersion.substring(1, 2)) < 3) {
          logger.error("Terminating request as sdkVersion is less than 3");
          hrh.setTerminationReason(ServletHandler.lowSdkVersion);
          InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
          hrh.responseSender.sendNoAdResponse(e);
          return;
        } else
          logger.debug("sdk-version : " + hrh.responseSender.sasParams.sdkVersion);
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
    if(ServletHandler.config.getBoolean("sendOnlyToWhitelist") == true) {
      List<String> whitelist = ServletHandler.config.getList("whitelist");
      if(null == whitelist || !whitelist.contains(hrh.responseSender.sasParams.siteId)) {
        logger.debug("site id not present in whitelist, so sending no ad response");
        hrh.responseSender.sendNoAdResponse(e);
        return;
      }
    }

    // getting the selected third party site details
    HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new MatchSegments(logger)
        .matchSegments(hrh.responseSender.sasParams);

    if(matchedSegments == null) {
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    // applying all the filters
    ChannelSegmentEntity[] rows = Filters.filter(matchedSegments, logger, 0.0, ServletHandler.config,
        ServletHandler.adapterConfig);

    if(rows == null || rows.length == 0) {
      hrh.responseSender.sendNoAdResponse(e);
      logger.debug("No Entities matching the request.");
      return;
    }

    List<ChannelSegment> segments = new ArrayList<ChannelSegment>();

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

    logger.debug("Total channels available for sending requests " + rows.length);
    hrh.responseSender.sasParams.lowestEcpm = getLowestEcpm(rows, logger);
    if (logger.isDebugEnabled()) {
      logger.debug("Lowest ecpm is " + hrh.responseSender.sasParams.lowestEcpm);
    }
    segments = AsyncRequestMaker.prepareForAsyncRequest(rows, logger, ServletHandler.config, ServletHandler.rtbConfig,
        ServletHandler.adapterConfig, hrh.responseSender, advertiserSet, e, ServletHandler.repositoryHelper,
        hrh.jObject, hrh.responseSender.sasParams);

    if(segments.isEmpty()) {
      logger.debug("No succesfull configuration of adapter ");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    List<ChannelSegment> tempRankList;
    tempRankList = Filters.rankAdapters(segments, logger, ServletHandler.config);
    tempRankList = Filters.ensureGuaranteedDelivery(tempRankList, ServletHandler.adapterConfig, logger);

    tempRankList = AsyncRequestMaker.makeAsyncRequests(tempRankList, logger, hrh.responseSender, e);

    hrh.responseSender.setRankList(tempRankList);

    if(logger.isDebugEnabled()) {
      logger.debug("Number of tpans whose request was successfully completed "
          + hrh.responseSender.getRankList().size());
    }
    // if none of the async request succeed, we return "NO_AD"
    if(hrh.responseSender.getRankList().isEmpty()) {
      logger.debug("No calls");
      hrh.responseSender.sendNoAdResponse(e);
      return;
    }

    // Resetting the rankIndexToProcess for already completed adapters.
    int rankIndexToProcess = hrh.responseSender.getRankIndexToProcess();
    ChannelSegment segment = hrh.responseSender.getRankList().get(rankIndexToProcess);
    while (segment.adNetworkInterface.isRequestCompleted()) {
      if(segment.adNetworkInterface.getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
        hrh.responseSender.sendAdResponse(segment.adNetworkInterface, e);
        break;
      }
      rankIndexToProcess++;
      if(rankIndexToProcess >= hrh.responseSender.getRankList().size()) {
        hrh.responseSender.sendNoAdResponse(e);
        break;
      }
      segment = hrh.responseSender.getRankList().get(rankIndexToProcess);
    }
    hrh.responseSender.setRankIndexToProcess(rankIndexToProcess);
    if(logger.isDebugEnabled()) {
      logger.debug("retunrd from send Response, ranklist size is " + hrh.responseSender.getRankList().size());
    }
  }

  @Override
  public String getName() {
    return "BackFill";
  }

  private static double getLowestEcpm(ChannelSegmentEntity[] channelSegmentEntities, DebugLogger logger) {
    double lowestEcpm = 0;
    for (ChannelSegmentEntity channelSegmentEntity : channelSegmentEntities) {
      if (null == ServletHandler.repositoryHelper.queryChannelSegmentFeedbackRepository(
          channelSegmentEntity.getAdgroupId())) {
        if (logger.isDebugEnabled())
          logger.debug("ChannelSegmentfeedback entity is null for adgpid id " + channelSegmentEntity.getAdgroupId());
        continue;
      }
      if (logger.isDebugEnabled())
        logger.debug("ecpm is " + ServletHandler.repositoryHelper.queryChannelSegmentFeedbackRepository(
          channelSegmentEntity.getAdgroupId()).geteCPM());
      lowestEcpm = lowestEcpm > ServletHandler.repositoryHelper.queryChannelSegmentFeedbackRepository(
          channelSegmentEntity.getAdgroupId()).geteCPM() ? ServletHandler.repositoryHelper
          .queryChannelSegmentFeedbackRepository(channelSegmentEntity.getAdgroupId()).geteCPM() : lowestEcpm;
    }
    return lowestEcpm;
  }
}
