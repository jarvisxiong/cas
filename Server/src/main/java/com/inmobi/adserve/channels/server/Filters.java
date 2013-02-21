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

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

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

  public static HashMap<String/* advertiserId */, String/* advertiserName */> advertiserIdtoNameMapping = new HashMap<String, String>();
  public static HashMap<String/* advertiserId */, HashSet<String/* siteIncId */>> whiteListedSites = new HashMap<String, HashSet<String>>();
  public static long lastRefresh;
  public static Random random;

  private HashMap<String, HashMap<String, ChannelSegment>> matchedSegments;
  private Configuration serverConfiguration;
  private Configuration adapterConfiguration;
  private SASRequestParameters sasParams;
  private double revenueWindow;
  private DebugLogger logger;

  public Filters(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments, Configuration serverConfiguration,
      Configuration adapterConfiguration, SASRequestParameters sasParams, DebugLogger logger) {
    this.matchedSegments = matchedSegments;
    this.serverConfiguration = serverConfiguration;
    this.adapterConfiguration = adapterConfiguration;
    this.sasParams = sasParams;
    this.revenueWindow = serverConfiguration.getDouble("revenueWindow", 0.33);
    this.logger = logger;
  }

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

  /**
   * Apply all the filters in this class and
   * 
   * @return returns a list of filtered channel segments
   */
  public List<ChannelSegment> applyFilters() {
    Filters.refreshWhiteListedSites(serverConfiguration, adapterConfiguration, logger);
    matchedSegments = advertiserLevelFiltering();
    matchedSegments = adGroupLevelFiltering();
    List<ChannelSegment> channelSegments = convertToSegmentsList(matchedSegments);
    return selectTopAdgroupsForRequest(channelSegments);
  }

  boolean isBurnLimitExceeded(ChannelSegment channelSegment) {
    boolean result = channelSegment.getChannelFeedbackEntity().getBalance() < channelSegment.getChannelFeedbackEntity()
        .getRevenue() * revenueWindow;
    String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
    if(result) {
      logger.debug("Burn limit exceeded by advertiser", advertiserId);
      if(advertiserIdtoNameMapping.containsKey(advertiserId)) {
        InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
            InspectorStrings.droppedInburnFilter);
      }
    } else {
      logger.debug("Burn limit filter passed by advertiser", advertiserId);
    }
    return result;
  }

  boolean isDailyImpressionCeilingExceeded(ChannelSegment channelSegment) {
    boolean result = channelSegment.getChannelFeedbackEntity().getTodayImpressions() > channelSegment
        .getChannelEntity().getImpressionCeil();
    String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
    if(result) {
      logger.debug("Impression limit exceeded by advertiser", advertiserId);
      if(advertiserIdtoNameMapping.containsKey(advertiserId)) {
        InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
            InspectorStrings.droppedInImpressionFilter);
      }
    } else {
      logger.debug("Impression limit filter passed by advertiser", advertiserId);
    }
    return result;
  }

  boolean isDailyRequestCapExceeded(ChannelSegment channelSegment) {
    boolean result = channelSegment.getChannelFeedbackEntity().getTodayRequests() > channelSegment.getChannelEntity()
        .getRequestCap();
    String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
    if(result) {
      logger.debug("Request Cap exceeded by advertiser", advertiserId);
      if(advertiserIdtoNameMapping.containsKey(advertiserId)) {
        InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
            InspectorStrings.droppedInImpressionFilter);
      }
    } else {
      logger.debug("Request Cap filter passed by advertiser", advertiserId);
    }
    return result;
  }

  boolean isSiteAbsentInWhiteList(String advertiserId, Random random) {
    boolean result = whiteListedSites.containsKey(advertiserId)
        && !whiteListedSites.get(advertiserId).contains(new Long(sasParams.siteIncId).toString())
        && random.nextInt(100) < 95;
    if(result) {
      logger.debug("Dropped in site whiteList filter advertiserId", advertiserId);
      InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
          InspectorStrings.droppedInSiteInclusionExclusionFilter);
    }
    return result;
  }

  /**
   * Filter that performs advertiser level filtering Drops all the segment of
   * the advertiser being filtered out
   * 
   * @return returns the map containing advertisers who has passed the filters
   */
  HashMap<String, HashMap<String, ChannelSegment>> advertiserLevelFiltering() {
    logger.debug("Inside advertiserLevelFiltering");
    HashMap<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String, ChannelSegment>>();
    ChannelSegment channelSegment;
    for (String advertiserId : matchedSegments.keySet()) {
      if(advertiserIdtoNameMapping.containsKey(advertiserId)) {
        InspectorStats.initializeFilterStats(advertiserIdtoNameMapping.get(advertiserId));
      }
      channelSegment = ((ChannelSegment[]) matchedSegments.get(advertiserId).values().toArray(new ChannelSegment[0]))[0];
      // dropping advertiser if balance is less than revenue of
      // that advertiser OR if todays impression is greater than impression
      // ceiling OR site is not present in advertiser's whiteList
      if(isBurnLimitExceeded(channelSegment) || isDailyImpressionCeilingExceeded(channelSegment)
          || isDailyRequestCapExceeded(channelSegment)
          || isSiteAbsentInWhiteList(channelSegment.getChannelSegmentEntity().getAdvertiserId(), random)) {
        continue;
      }
      // otherwise adding the advertiser to the list
      rows.put(advertiserId, matchedSegments.get(advertiserId));
    }
    printSegments(rows);
    return rows;
  }

  /**
   * Filter to perform adgroup level filtering and short list a configurable
   * number of adgroups per advertiser
   * 
   * @return returns the map containing adgroups left after filter and
   *         shortlisting
   */
  HashMap<String, HashMap<String, ChannelSegment>> adGroupLevelFiltering() {
    logger.debug("Inside adGroupLevelFiltering");
    HashMap<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String, ChannelSegment>>();
    for (String advertiserId : matchedSegments.keySet()) {
      HashMap<String, ChannelSegment> hashMap = new HashMap<String, ChannelSegment>();
      List<ChannelSegment> segmentListToBeSorted = new ArrayList<ChannelSegment>();
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        ChannelSegment channelSegment = matchedSegments.get(advertiserId).get(adgroupId);
        // applying siteFloor filter
        if(channelSegment.getChannelSegmentFeedbackEntity().geteCPM() < sasParams.siteFloor) {
          logger.debug("sitefloor filter failed by adgroup", channelSegment.getChannelSegmentFeedbackEntity().getId());
          continue;
        } else {
          logger.debug("sitefloor filter passed by adgroup", channelSegment.getChannelSegmentFeedbackEntity().getId());
        }
        // applying segment property filter
        if(isAnySegmentPropertyViolated(channelSegment.getChannelSegmentEntity())) {
          continue;
        }
        channelSegment.setPrioritisedECPM(getPrioritisedECPM(channelSegment));
        segmentListToBeSorted.add(channelSegment);
      }
      if(segmentListToBeSorted.isEmpty())
        continue;
      Collections.sort(segmentListToBeSorted, COMPARATOR);
      // choosing top segments from the sorted list
      int adGpCount = 1;
      int partnerSegmentNo;
      partnerSegmentNo = adapterConfiguration.getInt(advertiserIdtoNameMapping.get(advertiserId) + ".partnerSegmentNo",
          serverConfiguration.getInt("partnerSegmentNo", 2));
      logger.debug("PartnersegmentNo for advertiser", advertiserId, " is " + partnerSegmentNo);
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
    printSegments(rows);
    return rows;
  }

  /**
   * Segment property filter
   * 
   * @param channelSegmentEntity
   * @return
   */
  boolean isAnySegmentPropertyViolated(ChannelSegmentEntity channelSegmentEntity) {
    if(channelSegmentEntity.isUdIdRequired() && StringUtils.isEmpty(sasParams.uidParams)) {
      InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity.getAdvertiserId()),
          InspectorStrings.droppedInUdidFilter);
      return true;
    }
    if(channelSegmentEntity.isZipCodeRequired() && StringUtils.isEmpty(sasParams.postalCode)) {
      InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity.getAdvertiserId()),
          InspectorStrings.droppedInZipcodeFilter);
      return true;
    }
    if(channelSegmentEntity.isLatlongRequired() && StringUtils.isEmpty(sasParams.latLong)) {
      InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity.getAdvertiserId()),
          InspectorStrings.droppedInLatLongFilter);
      return true;
    }
    if(channelSegmentEntity.isRestrictedToRichMediaOnly() && !sasParams.isRichMedia) {
      InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity.getAdvertiserId()),
          InspectorStrings.droppedInLatLongFilter);
      return true;
    }
    return false;
  }

  /**
   * Method which selects a configurable number of top adgroups based on their
   * prioritised ecpm
   * 
   * @param rows
   *          list containing those adgroups
   * @return returns the list having top adgroups
   */
  List<ChannelSegment> selectTopAdgroupsForRequest(List<ChannelSegment> rows) {
    logger.debug("Inside selectTopAdgroupsForRequest Filter");
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

  /**
   * Method which returns prioritised ecpm
   * 
   * @param channelSegment
   * @param config
   * @return
   */
  double getPrioritisedECPM(ChannelSegment channelSegment) {
    double ecpm = channelSegment.getChannelSegmentFeedbackEntity().geteCPM();
    double eCPMShift = serverConfiguration.getDouble("ecpmShift", 0.1);
    double feedbackPower = serverConfiguration.getDouble("feedbackPower", 2.0);
    int priority = channelSegment.getChannelEntity().getPriority() < 5 ? 5 - channelSegment.getChannelEntity()
        .getPriority() : 1;
    return (Math.pow((ecpm + eCPMShift), feedbackPower) * (priority) * getECPMBoostFactor(channelSegment));
  }

  void printSegments(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments) {
    logger.debug("Segments are :");
    for (String adkey : matchedSegments.keySet()) {
      for (String gpkey : matchedSegments.get(adkey).keySet()) {
        logger.debug("Advertiser is",
            matchedSegments.get(adkey).get(gpkey).getChannelSegmentEntity().getAdvertiserId(), "and AdGp is",
            matchedSegments.get(adkey).get(gpkey).getChannelSegmentEntity().getAdgroupId(), "ecpm is", matchedSegments
                .get(adkey).get(gpkey).getPrioritisedECPM());
      }
    }
  }

  /**
   * Method which ranks segments in weighted random of their prioritised ecpm
   * 
   * @param segment
   *          list to be ranked
   * @return returns the ranked list
   */
  public ArrayList<ChannelSegment> rankAdapters(List<ChannelSegment> segment) {
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

  List<ChannelSegment> convertToSegmentsList(HashMap<String, HashMap<String, ChannelSegment>> matchedSegments) {
    ArrayList<ChannelSegment> segmentList = new ArrayList<ChannelSegment>();
    for (String advertiserId : matchedSegments.keySet()) {
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        segmentList.add(matchedSegments.get(advertiserId).get(adgroupId));
        logger.debug("ChannelSegment Added to list for advertiserid :", advertiserId, "and adgroupid", adgroupId);
      }
    }
    return segmentList;
  }

  private double getECPMBoostFactor(ChannelSegment channelSegment) {
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
   *          : List containing ranked segments }
   * 
   * @param adapterConfiguration
   * @param logger
   * @return
   */
  public List<ChannelSegment> ensureGuaranteedDelivery(List<ChannelSegment> rankList) {
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
