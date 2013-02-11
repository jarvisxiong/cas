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

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
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
      return o1.channelSegmentFeedbackEntity.getPrioritisedECPM()
          - o2.channelSegmentFeedbackEntity.getPrioritisedECPM() > 0.0 ? -1 : 1;
    }
  };

  private static RepositoryHelper repositoryHelper;
  public static HashMap<String, String> advertiserIdtoNameMapping = new HashMap<String, String>();
  public static HashMap<String, HashSet<String>> whiteListedSites = new HashMap<String, HashSet<String>>();
  public static long lastRefresh;
  public static Random random;

  // To boost ecpm of a parnter to meet the impression floor

  public static void init(Configuration adapterConfiguration, RepositoryHelper repositoryHelper) {
    Filters.repositoryHelper = repositoryHelper;
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

  public static ChannelSegment[] filter(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments,
      DebugLogger logger, Double siteFloor, Configuration serverConfiguration, Configuration adapterConfiguration,
      String siteId) {

    refreshWhiteListedSites(serverConfiguration, adapterConfiguration, logger);

    return segmentsPerRequestFilter(
        matchedSegments,
        convertToSegmentsArray(Filters.partnerSegmentCountFilter(
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

      if(null == channelSegment.channelEntity || null == channelSegment.channelFeedbackEntity) {
        logger.debug("Repo Exception/No entry in ChannelFeedbackRepository/ChannelRepository for advertiserID "
            + advertiserId);
      } else {

        if(channelSegment.channelFeedbackEntity.getBalance() < channelSegment.channelFeedbackEntity.getRevenue()
            * revenueWindow) {
          // dropping advertiser(all segments) if balance is less than
          // 10*revenue of that channel(advertiser)
          logger.debug("Burn limit exceeded by advertiser " + advertiserId);
          if(advertiserIdtoNameMapping.containsKey(advertiserId))
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                InspectorStrings.droppedInburnFilter);
          continue;
        }

        logger.debug("Burn limit filter passed by advertiser " + advertiserId);
        
        if(channelSegment.channelFeedbackEntity.getTodayImpressions() > channelSegment.channelEntity.getImpressionCeil()) {
          // dropping advertiser(all segments) if todays impression is greater
          // than impression ceiling
          logger.debug("Impression limit exceeded by advertiser " + advertiserId);
          if(advertiserIdtoNameMapping.containsKey(advertiserId))
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                InspectorStrings.droppedInImpressionFilter);
          continue;
        }

        logger.debug("Impression limit filter passed by advertiserId " + advertiserId);
      }      

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

        if(null == channelSegment.channelSegmentFeedbackEntity) {
          logger.debug("Error in retreiving from repo so setting ecpm to default value");
          channelSegment.channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(
              channelSegment.channelSegmentEntity.getId(), channelSegment.channelSegmentEntity.getAdgroupId(),
              serverConfiguration.getDouble("default.ecpm"), serverConfiguration.getDouble("default.fillratio"), 0, 0,
              0, 0);
        }

        if(channelSegment.channelSegmentFeedbackEntity.geteCPM() >= siteFloor) {
          logger.debug("sitefloor filter passed by adgroup " + channelSegment.channelSegmentFeedbackEntity.getId());
          segmentListToBeSorted.add(channelSegment);
        } else
          logger.debug("sitefloor filter failed by adgroup " + channelSegment.channelSegmentFeedbackEntity.getId());
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

        hashMap.put(channelSegment.channelSegmentFeedbackEntity.getId(), channelSegment);
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
  public static ChannelSegment[] segmentsPerRequestFilter(
      HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, ChannelSegment[] rows, DebugLogger logger,
      Configuration serverConfiguration) {

    logger.debug("Inside SegmentsPerRequestFilter");
    double eCPMShift = serverConfiguration.getDouble("ecpmShift", 0.1);
    double feedbackPower = serverConfiguration.getDouble("feedbackPower", 2.0);
    List<ChannelSegment> hashMapList = new ArrayList<ChannelSegment>();
    List<ChannelSegment> shortlistedRow = new ArrayList<ChannelSegment>();

    // Creating a sorted list of segments based on their ecpm
    for (ChannelSegment row : rows) {

      if(null == row.channelSegmentFeedbackEntity) {
        if(logger.isDebugEnabled())
          logger.debug("Error in retreiving from repo for adgprid " + row.channelSegmentEntity.getAdgroupId()
              + " and advertiserid " + row.channelSegmentEntity.getId() + " so setting ecpm to default");
        row.channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(row.channelSegmentEntity.getId(),
            row.channelSegmentEntity.getAdgroupId(), serverConfiguration.getDouble("default.ecpm"),
            serverConfiguration.getDouble("default.fillratio"), 0, 0, 0, 0);
      }

      // setting prioritisedECPM to take control of
      // shorlisting

      if(null == row.channelEntity) {
        logger.debug("channelid not found setting priority to 10");
        row.channelEntity = new ChannelEntity();
        row.channelEntity.setPriority(serverConfiguration.getInt("default.priority"));
      }

      row.channelSegmentFeedbackEntity.setPrioritisedECPM(Math.pow(
          (row.channelSegmentFeedbackEntity.geteCPM() + eCPMShift), feedbackPower)
          * (row.channelEntity.getPriority() < 5 ? 5 - row.channelEntity.getPriority() : 1)
          * getECPMBoostFactor(row.channelSegmentEntity.getId(), row.channelSegmentEntity.getChannelId(),
              row.channelSegmentEntity.getAdgroupId()));

      hashMapList.add(row);
    }

    Collections.sort(hashMapList, COMPARATOR);
    // choosing top segments from the sorted list
    int totalSegments = 0;
    int totalSegmentNo = serverConfiguration.getInt("totalSegmentNo");

    for (int i = 0; i < hashMapList.size(); i++) {
      ChannelSegment channelSegment = hashMapList.get(i);
      if(totalSegments < totalSegmentNo) {
        shortlistedRow.add(channelSegment);
        totalSegments++;
      } else if(advertiserIdtoNameMapping.containsKey(channelSegment.channelSegmentEntity.getAdvertiserId())) {
        InspectorStats.incrementStatCount(
            advertiserIdtoNameMapping.get(channelSegment.channelSegmentEntity.getAdvertiserId()),
            InspectorStrings.droppedInSegmentPerRequestFilter);
      }

    }

    logger.debug("Number of  ShortListed Segments are : " + shortlistedRow.size());

    for (int i = 0; i < shortlistedRow.size(); i++) {

      if(logger.isDebugEnabled())
        logger.debug("Segment with advertiserid "
            + shortlistedRow.get(i).channelSegmentFeedbackEntity.getAdvertiserId() + " adroupid "
            + shortlistedRow.get(i).channelSegmentFeedbackEntity.getId() + " Pecpm "
            + shortlistedRow.get(i).channelSegmentFeedbackEntity.getPrioritisedECPM());

    }

    return (ChannelSegment[]) shortlistedRow.toArray(new ChannelSegment[0]);
  }

  public static void printSegments(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger) {
    logger.debug("Segments are :");

    for (String adkey : matchedSegments.keySet()) {

      for (String gpkey : matchedSegments.get(adkey).keySet()) {

        try {

          if(logger.isDebugEnabled())
            logger.debug("Advertiser is " + matchedSegments.get(adkey).get(gpkey).channelSegmentEntity.getId()
                + " and AdGp is " + matchedSegments.get(adkey).get(gpkey).channelSegmentEntity.getAdgroupId()
                + " \tecpm is " + repositoryHelper.queryChannelSegmentFeedbackRepository(gpkey).geteCPM());

        } catch (NullPointerException e) {
          logger.debug("Repo Exception/No entry in ChannelSegmentFeedbackRepository for adgrpId : " + gpkey);
          continue;
        }

      }

    }

  }

  // creating ranks of shortlisted channelsegments ased on weighted random mean
  // of their prioritised ecpm
  public static ArrayList<ChannelSegment> rankAdapters(List<ChannelSegment> segment, DebugLogger logger,
      Configuration serverConfiguration) {

    int rank = 0;
    double eCPMShift = serverConfiguration.getDouble("ecpmShift", 0.1);
    double feedbackPower = serverConfiguration.getDouble("feedbackPower", 2.0);

    // Arraylist will contain the order in which we will wait for response
    // of the third party ad networks
    ArrayList<ChannelSegment> rankedList = new ArrayList<ChannelSegment>();

    if(segment.isEmpty())
      return rankedList;

    while (segment.size() > 1) {
      double totalPriority = 0.0;

      for (int index = 0; index < segment.size(); index++) {
        // setting the prioritised ecpm for this segment
        segment.get(index).channelSegmentFeedbackEntity.setPrioritisedECPM((Math.pow(
            (segment.get(index).channelSegmentFeedbackEntity.geteCPM() + eCPMShift), feedbackPower) * (segment
            .get(index).channelEntity.getPriority() < 5 ? (5 - segment.get(index).channelEntity.getPriority()) : 1))
            * getECPMBoostFactor(segment.get(index).channelSegmentEntity.getId(),
                segment.get(index).channelSegmentEntity.getChannelId(),
                segment.get(index).channelSegmentEntity.getAdgroupId()));
        segment.get(index).lowerPriorityRange = totalPriority;
        totalPriority += segment.get(index).channelSegmentFeedbackEntity.getPrioritisedECPM();
        segment.get(index).higherPriorityRange = totalPriority;
        logger.debug("total priority here is " + totalPriority);
      }

      double randomNumber = Math.random() * totalPriority;

      for (int index = 0; index < segment.size(); index++) {

        if(randomNumber >= segment.get(index).lowerPriorityRange
            && randomNumber <= segment.get(index).higherPriorityRange) {

          if(logger.isDebugEnabled())
            logger.debug("rank " + rank++ + " adapter has channel id " + segment.get(index).adNetworkInterface.getId());

          rankedList.add(segment.get(index));
          segment.remove(index);
          break;
        }

      }

    }

    if(logger.isDebugEnabled())
      logger.debug("rank " + rank++ + " adapter has channel id " + segment.get(0).adNetworkInterface.getId());

    rankedList.add(segment.get(0));
    logger.info("Ranked candidate adapters randomly");
    return rankedList;
  }

  private static ChannelSegment[] convertToSegmentsArray(
      HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, DebugLogger logger) {
    ArrayList<ChannelSegment> rows = new ArrayList<ChannelSegment>();

    for (String advertiserId : matchedSegments.keySet()) {

      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        rows.add(matchedSegments.get(advertiserId).get(adgroupId));
        logger.debug("ChannelSegmentEntity Added to array for advertiserid : " + advertiserId + " and adgroupid "
            + adgroupId);
      }

    }

    return (ChannelSegment[]) rows.toArray(new ChannelSegment[0]);
  }

  public static double getECPMBoostFactor(String advertiserId, String channelId, String adGroupId) {
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

  public static List<ChannelSegment> ensureGuaranteedDelivery(List<ChannelSegment> rankList,
      Configuration adapterConfiguration, DebugLogger logger) {
    logger.debug("Inside guaranteed delivery filter");
    List<ChannelSegment> newRankList = new ArrayList<ChannelSegment>();
    newRankList.add(rankList.get(0));

    for (int rank = 1; rank < rankList.size(); rank++) {
      ChannelSegment rankedSegment = rankList.get(rank);

      if(!adapterConfiguration.getString(rankedSegment.adNetworkInterface.getName() + ".gauranteedDelivery", "false")
          .equals("true")) {
        newRankList.add(rankedSegment);
      } else
        logger.debug("Dropping partner" + rankedSegment.adNetworkInterface.getName() + "rank " + rank
            + "due to guarnteed delivery");

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
