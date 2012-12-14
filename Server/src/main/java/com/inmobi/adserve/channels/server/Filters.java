package com.inmobi.adserve.channels.server;

import java.util.HashMap;

import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import com.inmobi.adserve.channels.entity.*;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.repository.*;
import com.inmobi.adserve.channels.server.HttpRequestHandler.ChannelSegment;

/**
 * 
 * @author devashish
 * 
 *         Filter class to filter the segments selected by MatchSegment class
 * 
 */

public class Filters {

  private static Comparator<ChannelSegmentFeedbackEntity> COMPARATOR = new Comparator<ChannelSegmentFeedbackEntity>() {
    public int compare(ChannelSegmentFeedbackEntity o1, ChannelSegmentFeedbackEntity o2) {
      return o1.getPrioritisedECPM() - o2.getPrioritisedECPM() > 0.0 ? -1 : 1;
    }
  };

  private static RepositoryHelper repositoryHelper;
  private static InspectorStats inspectorStat;
  private static AtomicInteger presentHour;
  public static HashMap<String, String> advertiserIdtoNameMapping = new HashMap<String, String>();

  // To boost ecpm of a parnter to meet the impression floor

  public static void init(Configuration adapterConfiguration, RepositoryHelper repositoryHelper, InspectorStats inspectorStat) {
    Filters.repositoryHelper = repositoryHelper;
    Filters.inspectorStat = inspectorStat;
    Iterator<String> itr = adapterConfiguration.getKeys();
    while (null != itr && itr.hasNext()) {
      String str = itr.next();
      if(str.endsWith(".advertiserId")) {
        advertiserIdtoNameMapping.put(adapterConfiguration.getString(str), str.replace(".advertiserId", ""));
      }
    }
    presentHour = new AtomicInteger(Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).get(Calendar.HOUR_OF_DAY));
  }

  /**
   * 
   * @param matchedSegments
   *          : The map containg advertiserid mapped to its map of adgroupid
   *          mapped to its segments
   * @param logger
   * @return
   */
  public static HashMap<String, HashMap<String, ChannelSegmentEntity>> impressionBurnFilter(
      HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments, DebugLogger logger, Configuration serverConfiguration) {

    logger.debug("Inside impressionBurnFilter");
    long todayImpressions;
    long impressionCeil;
    double balance;
    double revenue;
    String channelId;
    ChannelEntity channelEntity;
    ChannelFeedbackEntity channelFeedbackEntity;
    HashMap<String, HashMap<String, ChannelSegmentEntity>> rows = new HashMap<String, HashMap<String, ChannelSegmentEntity>>();
    double revenueWindow = serverConfiguration.getDouble("revenueWindow", 0.33);

    for (String advertiserId : matchedSegments.keySet()) {

      if(advertiserIdtoNameMapping.containsKey(advertiserId))
        inspectorStat.initializeFilterStats(advertiserIdtoNameMapping.get(advertiserId));

      channelId = ((ChannelSegmentEntity[]) matchedSegments.get(advertiserId).values().toArray(new ChannelSegmentEntity[0]))[0].getChannelId();
      channelEntity = repositoryHelper.queryChannelRepository(channelId);
      todayImpressions = -1;
      impressionCeil = -1;
      balance = -1;
      revenue = -1;
      channelFeedbackEntity = repositoryHelper.queryChannelFeedbackRepository(advertiserId);

      if(null != channelFeedbackEntity) {
        todayImpressions = channelFeedbackEntity.getTodayImpressions();
        balance = channelFeedbackEntity.getBalance();
        revenue = channelFeedbackEntity.getRevenue();
      }

      if(null != channelEntity)
        impressionCeil = channelEntity.getImpressionCeil();

      if(todayImpressions == -1 || impressionCeil == -1)
        logger.debug("Repo Exception/No entry in ChannelFeedbackRepository/ChannelRepository for advertiserID " + advertiserId);
      else {
        if(todayImpressions > impressionCeil) {
          // dropping advertiser(all segments) if todays impression is greater than impression ceiling
          logger.debug("Impression limit exceeded by advertiser " + advertiserId);
          if(advertiserIdtoNameMapping.containsKey(advertiserId))
            inspectorStat.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId), "filter_stat", InspectorStrings.droppedInImpressionFilter);
          continue;
        }
      }
      logger.debug("Impression limit filter passed by advertiserId " + advertiserId);

      if(balance == -1)
        logger.debug("Repo Exception/No entry in ChannelFeedbackRepository for advertiserID " + advertiserId);
      else {
        if(balance < revenue * revenueWindow) {
          // dropping advertiser(all segments) if balance is less than 10*revenue of that channel(advertiser)
          logger.debug("Burn limit exceeded by advertiser " + advertiserId);
          if(advertiserIdtoNameMapping.containsKey(advertiserId))
            inspectorStat.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId), "filter_stat", InspectorStrings.droppedInburnFilter);
          continue;
        }
        logger.debug("Burn limit filter passed by advertiser " + advertiserId + " "
            + repositoryHelper.queryChannelFeedbackRepository(advertiserId).getRevenue() * revenueWindow);
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
  public static HashMap<String, HashMap<String, ChannelSegmentEntity>> partnerSegmentCountFilter(
      HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments, Double siteFloor, DebugLogger logger, Configuration serverConfiguration,
      Configuration adapterConfiguration) {

    logger.debug("Inside PartnerSegmentCountFilter");
    HashMap<String, HashMap<String, ChannelSegmentEntity>> rows = new HashMap<String, HashMap<String, ChannelSegmentEntity>>();

    for (String advertiserId : matchedSegments.keySet()) {
      HashMap<String, ChannelSegmentEntity> hashMap = new HashMap<String, ChannelSegmentEntity>();
      List<ChannelSegmentFeedbackEntity> hashMapList = new ArrayList<ChannelSegmentFeedbackEntity>();
      
      // Creating a sorted list of segments based on their ecpm
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        ChannelSegmentEntity channelSegmentEntity = matchedSegments.get(advertiserId).get(adgroupId);
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper.queryChannelSegmentFeedbackRepository(adgroupId);
        if(null == channelSegmentFeedbackEntity) {
          logger.debug("Error in retreiving from repo so setting ecpm to default value");
          channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(channelSegmentEntity.getId(), 
                                                                          channelSegmentEntity.getAdgroupId(),
                                                                          serverConfiguration.getDouble("default.ecpm"), 
                                                                          serverConfiguration.getDouble("default.fillratio"));
        }
        if(channelSegmentFeedbackEntity.geteCPM() >= siteFloor) {
          logger.debug("sitefloor filter passed by adgroup " + channelSegmentFeedbackEntity.getId());
          hashMapList.add(channelSegmentFeedbackEntity);
        } else
          logger.debug("sitefloor filter failed by adgroup " + channelSegmentFeedbackEntity.getId());
      }
      if(hashMapList.isEmpty())
        continue;
      Collections.sort(hashMapList, COMPARATOR);
      // choosing top segments from the sorted list\
      int adGpCount = 1;
      int partnerSegmentNo;
      partnerSegmentNo = adapterConfiguration.getInt(advertiserIdtoNameMapping.get(advertiserId) + ".partnerSegmentNo",
                                                      serverConfiguration.getInt("partnerSegmentNo", 2));
      logger.debug("PartnersegmentNo for advertiser " + advertiserId + " is " + partnerSegmentNo);
      for (ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity : hashMapList) {
        if(adGpCount > partnerSegmentNo)
          break;
        hashMap.put(channelSegmentFeedbackEntity.getId(), matchedSegments.get(advertiserId).get(channelSegmentFeedbackEntity.getId()));
        adGpCount++;
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
  public static ChannelSegmentEntity[] segmentsPerRequestFilter(HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments,
      ChannelSegmentEntity[] rows, DebugLogger logger, Configuration serverConfiguration) {

    logger.debug("Inside SegmentsPerRequestFilter");
    double eCPMShift = serverConfiguration.getDouble("ecpmShift", 0.1);
    double feedbackPower = serverConfiguration.getDouble("feedbackPower", 2.0);
    List<ChannelSegmentFeedbackEntity> hashMapList = new ArrayList<ChannelSegmentFeedbackEntity>();
    List<ChannelSegmentEntity> shortlistedRow = new ArrayList<ChannelSegmentEntity>();

    // Creating a sorted list of segments based on their ecpm
    for (ChannelSegmentEntity row : rows) {
      ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
      channelSegmentFeedbackEntity = repositoryHelper.queryChannelSegmentFeedbackRepository(row.getAdgroupId());
      if(null == channelSegmentFeedbackEntity) {
        logger.debug("Error in retreiving from repo for adgprid " + row.getAdgroupId() + " and advertiserid " + row.getId() + " so setting ecpm to default");
        channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(row.getId(),
                                                                        row.getAdgroupId(), 
                                                                        serverConfiguration.getDouble("default.ecpm"),
                                                                        serverConfiguration.getDouble("default.fillratio"));
      }
      // setting prioritisedECPM to take control of
      // shorlisting
      ChannelEntity channelEntity;
      logger.debug("Getting priority. Channel id is :" + row.getChannelId());
      channelEntity = repositoryHelper.queryChannelRepository(row.getChannelId());
      if(null == channelEntity) {
        logger.debug("channelid not found setting priority to 10");
        channelEntity = new ChannelEntity();
        channelEntity.setPriority(serverConfiguration.getInt("default.priority"));
      }
      channelSegmentFeedbackEntity.setPrioritisedECPM( Math.pow((channelSegmentFeedbackEntity.geteCPM() + eCPMShift), feedbackPower)
                                                      * (channelEntity.getPriority() < 5 ? 5 - channelEntity.getPriority() : 1) 
                                                      * getECPMBoostFactor(row.getId(), row.getChannelId(), row.getAdgroupId()) );

      hashMapList.add(channelSegmentFeedbackEntity);
    }
    Collections.sort(hashMapList, COMPARATOR);
    // choosing top segments from the sorted list
    int totalSegments = 0;
    int totalSegmentNo = serverConfiguration.getInt("totalSegmentNo");
    for (int i = 0; i < hashMapList.size(); i++) {
      String advertiserId = hashMapList.get(i).getAdvertiserId();
      String adgroupId = hashMapList.get(i).getId();
      if(totalSegments < totalSegmentNo) {
        shortlistedRow.add(matchedSegments.get(advertiserId).get(adgroupId));
        totalSegments++;
      } else if(advertiserIdtoNameMapping.containsKey(advertiserId))
        inspectorStat.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId), "filter_stat", InspectorStrings.droppedInSegmentPerRequestFilter);
    }
    logger.debug("Number of  ShortListed Segments are : " + shortlistedRow.size());
    for (int i = 0; i < shortlistedRow.size(); i++) {
      Double eCPM;
      if(null != repositoryHelper.queryChannelSegmentFeedbackRepository(shortlistedRow.get(i).getAdgroupId()))
        eCPM = repositoryHelper.queryChannelSegmentFeedbackRepository(shortlistedRow.get(i).getAdgroupId()).getPrioritisedECPM();
      else {
        logger.debug("No entry in channelfeedbackrepo");
        eCPM = serverConfiguration.getDouble("default.ecpm");
      }
      logger.debug("Segment with advertiserid " + shortlistedRow.get(i).getId() + " adroupid " + shortlistedRow.get(i).getAdgroupId() + " Pecpm " + eCPM);
    }
    return (ChannelSegmentEntity[]) shortlistedRow.toArray(new ChannelSegmentEntity[0]);

  }

  public static void printSegments(HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments, DebugLogger logger) {
    logger.debug("Segments are :");
    for (String adkey : matchedSegments.keySet()) {
      for (String gpkey : matchedSegments.get(adkey).keySet()) {
        try {
          logger.debug("Advertiser is " + matchedSegments.get(adkey).get(gpkey).getId() + " and AdGp is "
              + matchedSegments.get(adkey).get(gpkey).getAdgroupId() + " \tecpm is " + repositoryHelper.queryChannelSegmentFeedbackRepository(gpkey).geteCPM());
        } catch (NullPointerException e) {
          logger.debug("Repo Exception/No entry in ChannelSegmentFeedbackRepository for adgrpId : " + gpkey);
          continue;
        }
      }
    }
  }

  // creating ranks of shortlisted channelsegments ased on weighted random mean
  // of their prioritised ecpm
  public static ArrayList<ChannelSegment> rankAdapters(List<ChannelSegment> segment, DebugLogger logger, Configuration serverConfiguration) {

    Random random = new Random();
    int rank = 0;
    double eCPMShift = serverConfiguration.getDouble("ecpmShift", 0.1);
    double feedbackPower = serverConfiguration.getDouble("feedbackPower", 2.0);

    // Arraylist will contain the order in which we will wait for response
    // of the third party ad networks
    ArrayList<ChannelSegment> arrayList = new ArrayList();
    while (segment.size() > 1) {
      double totalPriority = 0.0;
      for (int index = 0; index < segment.size(); index++) {
        // setting the prioritised ecpm for this segment
        segment.get(index).channelSegmentFeedbackEntity.setPrioritisedECPM((Math.pow((segment.get(index).channelSegmentFeedbackEntity.geteCPM() + eCPMShift),
            feedbackPower) * (segment.get(index).channelEntity.getPriority() < 5 ? (5 - segment.get(index).channelEntity.getPriority()) : 1))
            * getECPMBoostFactor(segment.get(index).channelSegmentEntity.getId(), segment.get(index).channelSegmentEntity.getChannelId(),
                segment.get(index).channelSegmentEntity.getAdgroupId()));

        segment.get(index).lowerPriorityRange = totalPriority;
        totalPriority += segment.get(index).channelSegmentFeedbackEntity.getPrioritisedECPM();
        segment.get(index).higherPriorityRange = totalPriority;
        {
          logger.debug("total priority here is " + totalPriority);
        }
      }
      double randomNumber = Math.random() * totalPriority;
      for (int index = 0; index < segment.size(); index++) {
        if(randomNumber >= segment.get(index).lowerPriorityRange && randomNumber <= segment.get(index).higherPriorityRange) {
          {
            logger.debug("rank " + rank++ + " adapter has channel id " + segment.get(index).adNetworkInterface.getId());
          }
          arrayList.add(segment.get(index));
          segment.remove(index);
          break;
        }
      }
    }
    logger.debug("rank " + rank++ + " adapter has channel id " + segment.get(0).adNetworkInterface.getId());
    arrayList.add(segment.get(0));
    logger.info("Ranked candidate adapters randomly");
    return arrayList;
  }

  public static double getECPMBoostFactor(String advertiserId, String channelId, String adGroupId) {
    long impressions = 0;
    long floor = 0;
    double fillRatio = 0;
    double ecpm = 0;
    if(null != repositoryHelper.queryChannelFeedbackRepository(advertiserId))
      impressions = repositoryHelper.queryChannelFeedbackRepository(advertiserId).getTodayImpressions();
    if(null != repositoryHelper.queryChannelSegmentFeedbackRepository(adGroupId)) {
      fillRatio = repositoryHelper.queryChannelSegmentFeedbackRepository(adGroupId).getFillRatio();
      ecpm = repositoryHelper.queryChannelSegmentFeedbackRepository(adGroupId).geteCPM();
    }
    if(null != repositoryHelper.queryChannelRepository(channelId))
      floor = repositoryHelper.queryChannelRepository(channelId).getImpressionFloor();
    int currentHour = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).get(Calendar.HOUR_OF_DAY);
    int lastHour = presentHour.get();
    if(currentHour > lastHour || (currentHour == 0 && lastHour == 23))
      presentHour.set(currentHour);
    if(impressions < floor / 24 * currentHour) {
      return 1 + ((floor - impressions) / floor) * fillRatio * ecpm * (currentHour + 1);
    }
    return 1;
  }

}
