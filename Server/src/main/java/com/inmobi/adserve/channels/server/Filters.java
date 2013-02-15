package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

/**
 * 
 * @author devashish
 * 
 *         Filter class to filter the segments selected by MatchSegment class
 * 
 */

public class Filters {

  private static Comparator<ChannelSegment> COMPARATOR = new Comparator<ChannelSegment>() {
    public int compare(ChannelSegment o1, ChannelSegment o2) {
      return o1.getPrioritisedECPM() > o2.getPrioritisedECPM() ? -1 : 1;
    }
  };

  public static HashMap<String, String> advertiserIdtoNameMapping = new HashMap<String, String>();
  public static HashMap<String, HashSet<String>> whiteListedSites = new HashMap<String, HashSet<String>>();
  public static long lastRefresh;
  public static Random random;

  // To boost ecpm of a parnter to meet the impression floor

  public static void init(Configuration adapterConfiguration) {
    Iterator<String> itr = adapterConfiguration.getKeys();

    while (null != itr && itr.hasNext()) {
      String str = itr.next();

      if(str.endsWith(".advertiserId")) {
        advertiserIdtoNameMapping.put(adapterConfiguration.getString(str), str.replace(".advertiserId", ""));
        String sites = adapterConfiguration.getString(str.replace(".advertiserId", ".whiteListedSites"));
        HashSet<String> siteSet = new HashSet<String>();

        if(!StringUtils.isEmpty(sites)) {
          siteSet.addAll(Arrays.asList(sites.split(",")));
          whiteListedSites.put(adapterConfiguration.getString(str), siteSet);
        }

      }

    }

    lastRefresh = System.currentTimeMillis();
    random = new Random();
  }

  public static List<ChannelSegment> filter(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments,
      DebugLogger logger, Double siteFloor, Configuration serverConfiguration, Configuration adapterConfiguration,
      String siteId) {

    refreshWhiteListedSites(serverConfiguration, adapterConfiguration, logger);

    return segmentsPerRequestFilter(
        matchedSegments,
        convertToSegmentsList(Filters.partnerSegmentCountFilter(
            Filters.impressionBurnFilter(matchedSegments, logger, serverConfiguration, siteId), siteFloor, logger,
            serverConfiguration, adapterConfiguration), logger), logger, serverConfiguration);
  }

  /**
   * 
   * @param matchedSegments
   *          : The map containg advertiserid mapped to its map of adgroupid
   *          mapped to its segments
   * @param logger
   * @return
   */
  public static HashMap<String, HashMap<String, ChannelSegment>> impressionBurnFilter(
      HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger,
      Configuration serverConfiguration, String siteId) {

    logger.debug("Inside impressionBurnFilter");
    HashMap<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String, ChannelSegment>>();
    ChannelSegment channelSegment;
    double revenueWindow = serverConfiguration.getDouble("revenueWindow", 0.33);

    for (String advertiserId : matchedSegments.keySet()) {

      if(advertiserIdtoNameMapping.containsKey(advertiserId))
        InspectorStats.initializeFilterStats(advertiserIdtoNameMapping.get(advertiserId));

      channelSegment = ((ChannelSegment[]) matchedSegments.get(advertiserId).values().toArray(new ChannelSegment[0]))[0];

      // dropping advertiser(all segments) if balance is less than revenue of
      // that channel(advertiser)
      if(channelSegment.getChannelFeedbackEntity().getBalance() < channelSegment.getChannelFeedbackEntity()
          .getRevenue() * revenueWindow) {
        logger.debug("Burn limit exceeded by advertiser " + advertiserId);

        if(advertiserIdtoNameMapping.containsKey(advertiserId))
          InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
              InspectorStrings.droppedInburnFilter);

        continue;
      }
      logger.debug("Burn limit filter passed by advertiser " + advertiserId);

      // dropping advertiser(all segments) if todays impression is greater
      // than impression ceiling
      if(channelSegment.getChannelFeedbackEntity().getTodayImpressions() > channelSegment.getChannelEntity()
          .getImpressionCeil()) {
        logger.debug("Impression limit exceeded by advertiser " + advertiserId);

        if(advertiserIdtoNameMapping.containsKey(advertiserId))
          InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
              InspectorStrings.droppedInImpressionFilter);

        continue;
      }
      logger.debug("Impression limit filter passed by advertiserId " + advertiserId);

      // dropping advertiser if the site is not in its whiltelist
      if(whiteListedSites.containsKey(advertiserId) && !whiteListedSites.get(advertiserId).contains(siteId)
          && random.nextInt(100) < 95) {
        logger.debug("Dropped in site inclusion-exclusion");
        InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
            InspectorStrings.droppedInSiteInclusionExclusionFilter);
        continue;
      }

      // otherwise adding the advertiser to the list
      rows.put(advertiserId, matchedSegments.get(advertiserId));
    }

    printSegments(rows, logger);
    return rows;

  }

  /**
   * Filter to short list a configurable number of segments per partner
   * 
   * @param matchedSegments
   *          : The map containing advertiserid mapped to its map of adgroupid
   *          mapped to its segments
   * @param siteFloor
   *          : lowest ecpm segment that can be served for that request
   * @param logger
   * @return
   */
  public static HashMap<String, HashMap<String, ChannelSegment>> partnerSegmentCountFilter(
      HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, Double siteFloor, DebugLogger logger,
      Configuration serverConfiguration, Configuration adapterConfiguration) {

    logger.debug("Inside PartnerSegmentCountFilter");
    HashMap<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String, ChannelSegment>>();

    for (String advertiserId : matchedSegments.keySet()) {
      HashMap<String, ChannelSegment> hashMap = new HashMap<String, ChannelSegment>();
      List<ChannelSegment> segmentListToBeSorted = new ArrayList<ChannelSegment>();

      // Creating a sorted list of segments based on their ecpm
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        ChannelSegment channelSegment = matchedSegments.get(advertiserId).get(adgroupId);

        if(channelSegment.getChannelSegmentFeedbackEntity().geteCPM() >= siteFloor) {
          logger.debug("sitefloor filter passed by adgroup", channelSegment.getChannelSegmentFeedbackEntity().getId());
          segmentListToBeSorted.add(channelSegment);
        } else
          logger.debug("sitefloor filter failed by adgroup", channelSegment.getChannelSegmentFeedbackEntity().getId());

      }

      if(segmentListToBeSorted.isEmpty())
        continue;

      Collections.sort(segmentListToBeSorted, COMPARATOR);
      // choosing top segments from the sorted list\
      int adGpCount = 1;
      int partnerSegmentNo;
      partnerSegmentNo = adapterConfiguration.getInt(advertiserIdtoNameMapping.get(advertiserId) + ".partnerSegmentNo",
          serverConfiguration.getInt("partnerSegmentNo", 2));

      if(logger.isDebugEnabled())
        logger.debug("PartnersegmentNo for advertiser " + advertiserId + " is " + partnerSegmentNo);

      for (ChannelSegment channelSegment : segmentListToBeSorted) {

        if(adGpCount > partnerSegmentNo) {
          break;
        }

        hashMap.put(channelSegment.getChannelSegmentFeedbackEntity().getId(), channelSegment);
        adGpCount++;

        if(advertiserIdtoNameMapping.containsKey(advertiserId)) {
          InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
              InspectorStrings.totalSelectedSegments);
        }

      }

      rows.put(advertiserId, hashMap);
    }

    printSegments(rows, logger);
    return rows;

  }

  /**
   * 
   * @param matchedSegments
   *          : The map containg advertiserid mapped to its map of adgroupid
   *          mapped to its segments
   * @param rows
   *          : Array list of channelsegmententities containg segments
   *          shortlisted by impressionBurnFilter and partnerSegmentCountFilter
   * @param logger
   * @return
   */
  public static List<ChannelSegment> segmentsPerRequestFilter(
      HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, List<ChannelSegment> rows, DebugLogger logger,
      Configuration serverConfiguration) {

    logger.debug("Inside SegmentsPerRequestFilter");
    logger.debug(rows.size());
    List<ChannelSegment> shortlistedRow = new ArrayList<ChannelSegment>();
    // Creating a sorted list of segments based on their ecpm
    Collections.sort(rows, COMPARATOR);
    // choosing top segments from the sorted list
    int totalSegments = 0;
    int totalSegmentNo = serverConfiguration.getInt("totalSegmentNo");

    for (int i = 0; i < rows.size(); i++) {
      ChannelSegment channelSegment = rows.get(i);

      if(totalSegments < totalSegmentNo) {
        shortlistedRow.add(channelSegment);
        totalSegments++;
      } else if(advertiserIdtoNameMapping.containsKey(channelSegment.getChannelSegmentEntity().getAdvertiserId())) {
        InspectorStats.incrementStatCount(
            advertiserIdtoNameMapping.get(channelSegment.getChannelSegmentEntity().getAdvertiserId()),
            InspectorStrings.droppedInSegmentPerRequestFilter);
      }

    }

    logger.debug("Number of  ShortListed Segments are : " + shortlistedRow.size());

    for (int i = 0; i < shortlistedRow.size(); i++) {

      if(logger.isDebugEnabled()) {
        logger.debug("Segment with advertiserid " + shortlistedRow.get(i).getChannelSegmentEntity().getAdvertiserId()
            + " adroupid " + shortlistedRow.get(i).getChannelSegmentEntity().getAdgroupId() + " Pecpm "
            + shortlistedRow.get(i).getPrioritisedECPM());
      }
    }

    return shortlistedRow;
  }

  private static double getPrioritisedECPM(ChannelSegment channelSegment, Configuration config) {

    double ecpm = channelSegment.getChannelSegmentFeedbackEntity().geteCPM();
    double eCPMShift = config.getDouble("ecpmShift", 0.1);
    double feedbackPower = config.getDouble("feedbackPower", 2.0);
    int priority = channelSegment.getChannelEntity().getPriority() < 5 ? 5 - channelSegment.getChannelEntity()
        .getPriority() : 1;

    return (Math.pow((ecpm + eCPMShift), feedbackPower) * (priority) * getECPMBoostFactor(channelSegment));
  }

  public static void printSegments(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger) {
    logger.debug("Segments are :");

    for (String adkey : matchedSegments.keySet()) {

      for (String gpkey : matchedSegments.get(adkey).keySet()) {
        logger.debug("Advertiser is", matchedSegments.get(adkey).get(gpkey).getChannelSegmentEntity().getId(),
            "and AdGp is", matchedSegments.get(adkey).get(gpkey).getChannelSegmentEntity().getAdgroupId(), "ecpm is",
            matchedSegments.get(adkey).get(gpkey).getPrioritisedECPM());
      }

    }

  }

  // creating ranks of shortlisted channelsegments based on weighted random mean
  // of their prioritised ecpm
  public static ArrayList<ChannelSegment> rankAdapters(List<ChannelSegment> segment, DebugLogger logger,
      Configuration serverConfiguration) {

    int rank = 0;
    // Arraylist that will contain the order in which we will wait for response
    // of the third party ad networks
    ArrayList<ChannelSegment> rankedList = new ArrayList<ChannelSegment>();

    if(segment.isEmpty()) {
      return rankedList;
    }

    while (segment.size() > 1) {
      double totalPriority = 0.0;

      for (int index = 0; index < segment.size(); index++) {
        // setting the prioritised ecpm for this segment
        totalPriority += segment.get(index).getPrioritisedECPM();

      }

      double randomNumber = Math.random() * totalPriority;

      for (int index = 0; index < segment.size(); index++) {

        if(randomNumber > segment.get(index).getPrioritisedECPM()) {
          randomNumber -= segment.get(index).getPrioritisedECPM();
        } else {
          logger.debug("rank", rank++, "adapter has channel id", segment.get(index).getAdNetworkInterface().getId());
          rankedList.add(segment.get(index));
          segment.remove(index);
          break;
        }

      }

    }

    logger.debug("rank", rank++, "adapter has channel id", segment.get(0).getAdNetworkInterface().getId());
    rankedList.add(segment.get(0));
    logger.info("Ranked candidate adapters randomly");
    return rankedList;
  }

  private static List<ChannelSegment> convertToSegmentsList(
      HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger) {
    ArrayList<ChannelSegment> segmentList = new ArrayList<ChannelSegment>();

    for (String advertiserId : matchedSegments.keySet()) {

      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        segmentList.add(matchedSegments.get(advertiserId).get(adgroupId));
        logger.debug("ChannelSegment Added to list for advertiserid :", advertiserId, "and adgroupid", adgroupId);
      }

    }

    return segmentList;
  }

  public static double getECPMBoostFactor(ChannelSegment channelSegment) {
    /*
     * long impressions = 0; long floor = 0; double fillRatio = 0; double ecpm =
     * 0; if(null !=
     * repositoryHelper.queryChannelFeedbackRepository(advertiserId))
     * impressions =
     * repositoryHelper.queryChannelFeedbackRepository(advertiserId
     * ).getTodayImpressions(); if(null !=
     * repositoryHelper.queryChannelSegmentFeedbackRepository(adGroupId)) {
     * fillRatio =
     * repositoryHelper.queryChannelSegmentFeedbackRepository(adGroupId
     * ).getFillRatio(); ecpm =
     * repositoryHelper.queryChannelSegmentFeedbackRepository
     * (adGroupId).geteCPM(); } if(null !=
     * repositoryHelper.queryChannelRepository(channelId)) floor =
     * repositoryHelper.queryChannelRepository(channelId).getImpressionFloor();
     * long currentHour = (System.currentTimeMillis()%86400000)/3600000;
     * if(impressions < floor / 24 * currentHour) { return 1 + ((floor -
     * impressions) / floor) * fillRatio * ecpm * (currentHour + 1); }
     */
    return 1;
  }

  /**
   * Guaranteed delivery Filter to drop drop an advertiser if it has guaranteed
   * delivery enabled and not on top in ranklist
   * 
   * @param rankList
   *          : List containing ranked segments
   * @param adapterConfiguration
   * @param logger
   * @return
   */
  public static List<ChannelSegment> ensureGuaranteedDelivery(List<ChannelSegment> rankList,
      Configuration adapterConfiguration, DebugLogger logger) {
    logger.debug("Inside guaranteed delivery filter");
    List<ChannelSegment> newRankList = new ArrayList<ChannelSegment>();
    newRankList.add(rankList.get(0));

    for (int rank = 1; rank < rankList.size(); rank++) {
      ChannelSegment rankedSegment = rankList.get(rank);

      if(!adapterConfiguration.getString(rankedSegment.getAdNetworkInterface().getName() + ".gauranteedDelivery",
          "false").equals("true")) {
        newRankList.add(rankedSegment);
      } else {
        logger.debug("Dropping partner", rankedSegment.getAdNetworkInterface().getName(), "rank", rank,
            "due to guarnteed delivery");
      }

    }

    logger.debug("New ranklist size :" + newRankList.size());
    return newRankList;
  }

  public synchronized static void refreshWhiteListedSites(Configuration serverConfiguration,
      Configuration adapterConfiguration, DebugLogger logger) {

    if(System.currentTimeMillis() - lastRefresh < serverConfiguration.getInt("whiteListedSitesRefreshtime", 1000 * 300))
      return;

    logger.debug("refreshing whiteListedSites");
    Iterator<String> itr = adapterConfiguration.getKeys();

    while (null != itr && itr.hasNext()) {
      String str = itr.next();

      if(str.endsWith(".advertiserId")) {
        String sites = adapterConfiguration.getString(str.replace(".advertiserId", ".whiteListedSites"));
        HashSet<String> siteSet = new HashSet<String>();

        if(StringUtils.isEmpty(sites)) {
          whiteListedSites.remove(adapterConfiguration.getString(str));
        } else {
          siteSet.addAll(Arrays.asList(sites.split(",")));
          whiteListedSites.put(adapterConfiguration.getString(str), siteSet);
        }

      }

    }

    lastRefresh = System.currentTimeMillis();
  }
}
