package com.inmobi.adserve.channels.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

import java.util.concurrent.ConcurrentHashMap;

import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.AdResponse;
import com.inmobi.casthrift.CasChannelLog;
import com.inmobi.casthrift.RequestParams;
import com.inmobi.casthrift.RequestTpan;
import com.inmobi.casthrift.SiteParams;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.casthrift.Ad;
import com.inmobi.casthrift.AdIdChain;
import com.inmobi.casthrift.AdMeta;
import com.inmobi.casthrift.ContentRating;
import com.inmobi.casthrift.Gender;
import com.inmobi.casthrift.Geo;
import com.inmobi.casthrift.HandsetMeta;
import com.inmobi.casthrift.InventoryType;
import com.inmobi.casthrift.PricingModel;
import com.inmobi.casthrift.AdRR;
import com.inmobi.casthrift.Impression;
import com.inmobi.casthrift.Request;
import com.inmobi.casthrift.User;

import com.inmobi.casthrift.CasAdvertisementLog;

import com.inmobi.adserve.channels.util.DebugLogger;

public class Logging {

  private static AbstractMessagePublisher dataBusPublisher;
  private static String rrLogKey;
  private static String channelLogKey;
  private static String sampledAdvertisementLogKey;
  private static boolean enableFileLogging;
  private static boolean enableDatabusLogging;
  private final static ConcurrentHashMap<String, String> sampledAdvertiserLogNos = new ConcurrentHashMap<String, String>(2000);
  
  public static ConcurrentHashMap<String, String> getSampledadvertiserlognos() {
    return sampledAdvertiserLogNos;
  }

  private static int totalCount;

  public static void init(AbstractMessagePublisher dataBusPublisher, String rrLogKey, String channelLogKey,
      String advertisementLogKey, Configuration config) {
    Logging.dataBusPublisher = dataBusPublisher;
    Logging.rrLogKey = rrLogKey;
    Logging.channelLogKey = channelLogKey;
    Logging.sampledAdvertisementLogKey = advertisementLogKey;
    enableFileLogging = config.getBoolean("enableFileLogging");
    enableDatabusLogging = config.getBoolean("enableDatabusLogging");
    totalCount = config.getInt("sampledadvertisercount");
  }

  public static JSONArray getCarrier(JSONObject jObject) {
    try {
      return (jObject.getJSONArray("carrier"));
    } catch (JSONException e) {
      return null;
    }
  }

  public static JSONArray getHandset(JSONObject jObject) {
    try {
      return (jObject.getJSONArray("handset"));
    } catch (JSONException e) {
      return null;
    }
  }

  void appendToLog(StringBuilder log, String separator, String key, String value) {
    if(value == null) {
      return;
    }
    log.append(separator).append(key).append(value);
  }
  // Writing rrlogs
  public static void rrLogging(ChannelSegment channelSegment, DebugLogger logger, Configuration config,
      SASRequestParameters sasParams, String terminationReason) throws JSONException, TException {
    Logger rrLogger = Logger.getLogger(config.getString("rr"));
    boolean isTerminated = false;
    if(terminationReason.equalsIgnoreCase("no"))
      isTerminated = true;
    logger.info("Obtained the handle to rr logger");
    char separator = 0x01;
    StringBuilder log = new StringBuilder();
    short adsServed = 0;
    String host = null;
    try {
      InetAddress addr = InetAddress.getLocalHost();
      host = addr.getHostName();

      if(host == null) {
        logger.error("host cant be empty, abandoning rr logging");
        return;
      }
      log.append("host=\"" + host + "\"");
    } catch (UnknownHostException ex) {
      logger.error("could not resolve host inside rr logging, so abandoning response");
      return;
    }

    log.append(separator + "terminated=\"" + terminationReason + "\"");
    if(logger.isDebugEnabled())
      logger.debug("is sas params null here " + (sasParams == null));

    if(null != sasParams && null != sasParams.getSiteId())
      log.append(separator + "rq-mk-siteid=\"" + sasParams.getSiteId() + "\"");
    if(null != sasParams && null != sasParams.getRqMkAdcount())
      log.append(separator + "rq-mk-adcount=\"" + sasParams.getRqMkAdcount() + "\"");
    if(null != sasParams && null != sasParams.getTid())
      log.append(separator + "tid=\"" + sasParams.getTid() + "\"");

    InventoryType inventory = getInventoryType(sasParams);
    String timestamp = ReportTime.getTTime();
    log.append(separator + "ttime=\"" + timestamp + "\"");
    log.append(separator + "rq-src=[\"uk\",\"uk\",\"uk\",\"uk\",");
    if(null != sasParams && null != sasParams.getTp())
      log.append("\"" + sasParams.getTp() + "\"]");
    else
      log.append("\"dir\"]");

    log.append(separator + "selectedads=[");
    AdIdChain adChain = null;
    AdMeta adMeta = null;
    Ad ad = null;
    Impression impression = null;
    if(channelSegment != null) {
      InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
          InspectorStrings.serverImpression);
      adsServed = 1;
      ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
      log.append("{\"ad\":[");
      log.append(channelSegmentEntity.getIncId()).append(",");
      log.append("\"\",\"");
      adChain = new AdIdChain(channelSegmentEntity.getAdId(), channelSegmentEntity.getAdgroupId(),
          channelSegmentEntity.getCampaignId(), channelSegmentEntity.getAdvertiserId(), channelSegmentEntity.getExternalSiteKey());
      log.append(channelSegmentEntity.getAdId()).append("\",\"");
      log.append(channelSegmentEntity.getAdgroupId()).append("\",\"");
      log.append(channelSegmentEntity.getCampaignId()).append("\",\"");
      log.append(channelSegmentEntity.getAdvertiserId()).append("\",\"");
      ContentRating contentRating = getContentRating(sasParams);
      PricingModel pricingModel = getPricingModel(channelSegmentEntity.getPricingModel());
      adMeta = new AdMeta(contentRating, pricingModel, "BANNER");
      ad = new Ad(adChain, adMeta);
      impression = new Impression(channelSegment.getAdNetworkInterface().getImpressionId(), ad);
      log.append(channelSegmentEntity.getPricingModel()).append("\",\"BANNER\", \"");
      log.append(channelSegmentEntity.getExternalSiteKey()).append("\"],\"impid\":\"");
      log.append(channelSegment.getAdNetworkInterface().getImpressionId()).append("\"");
      double winBid = channelSegment.getAdNetworkInterface().getSecondBidPrice();
      if(winBid != -1) {
        log.append(",\"" + "winBid" + "\":\"" + winBid + "\"");
        ad.setWinBid(winBid);
      }
      log.append("}");
    }
    log.append("]");

    JSONArray handset = null;
    JSONArray carrier = null;
    String requestSlot = null;
    String slotServed = null;
    if(null != sasParams) {
      handset = sasParams.getHandset();
      carrier = sasParams.getCarrier();
      requestSlot = sasParams.getRqMkSlot();
      slotServed = sasParams.getSlot();
    }
    HandsetMeta handsetMeta = new HandsetMeta();
    if(null != handset)
      log.append(separator).append("handset=").append(handset);
    if(null != handset && handset.length() > 3) {
      handsetMeta.setId(handset.getInt(3));
      handsetMeta.setManufacturer(handset.getInt(2));
    } else if(null != sasParams && sasParams.getOsId() != 0)
      handsetMeta.setOsId(sasParams.getOsId());
    Geo geo = null;
    if(null != carrier) {
      log.append(separator).append("carrier=").append(carrier);
      geo = new Geo(carrier.getInt(0), Integer.valueOf(carrier.getInt(1)).shortValue());
      if(carrier.length() >= 4 && carrier.get(3) != null)
        geo.setRegion(carrier.getInt(3));
      if(carrier.length() >= 5 && carrier.get(4) != null)
        geo.setCity(carrier.getInt(4));
    }

    short slotRequested = -1;
    if(null != requestSlot) {
      log.append(separator).append("rq-mk-ad-slot=\"").append(requestSlot).append("\"");
      if(requestSlot.matches("^\\d+$"))
        slotRequested = Integer.valueOf(requestSlot).shortValue();
      else
        logger.error("wrong value for request slot is " + requestSlot);
    }

    if(null != slotServed) {
      log.append(separator).append("slot-served=").append(slotServed);
    }

    User user = new User();
    log.append(separator + "uparams={");
    if(null != sasParams) {
      if(null != sasParams.getAge()) {
        log.append("\"u-age\":\"").append(sasParams.getAge()).append("\",");
        if(sasParams.getAge().matches("^\\d+$"))
          user.setAge(Short.valueOf(sasParams.getAge()));
      }
      if(null != sasParams.getGender()) {
        log.append("\"u-gender\":\"").append(sasParams.getGender()).append("\",");
        user.setGender(getGender(sasParams));
      }
      if(null != sasParams.getGenderOrig())
        log.append("\"u-gender-orig\":\"").append(sasParams.getGenderOrig()).append("\",");
      if(null != sasParams.getUid()) {
        log.append("\"u-id\":\"").append(sasParams.getUid()).append("\",");
        user.setId(sasParams.getUid());
      }
      if(null != sasParams.getUserLocation())
        log.append("\"u-location\":\"").append(sasParams.getUserLocation()).append("\",");
      if(null != sasParams.getPostalCode())
        log.append("\"u-postalcode\":\"").append(sasParams.getPostalCode()).append("\"");
    }
    if(log.charAt(log.length() - 1) == ',')
      log.deleteCharAt(log.length() - 1);
    log.append("}").append(separator).append("u-id-params=");
    if(null != sasParams && null != sasParams.getUidParams())
      log.append(sasParams.getUidParams());
    else
      log.append("{}");

    if(null != sasParams && null != sasParams.getSiteSegmentId())
      log.append(separator).append("sel-seg-id=").append(sasParams.getSiteSegmentId());

    if(logger.isDebugEnabled())
      logger.debug("finally writing to rr log" + log.toString());

    if(enableFileLogging)
      rrLogger.info(log.toString());
    else
      logger.debug("file logging is not enabled");
    short adRequested = 1;
    Request request = new Request(adRequested, adsServed, sasParams == null ? null : sasParams.getSiteId(),
        sasParams == null ? null : sasParams.getTid());
    if(slotServed != null)
      request.setSlot_served(Integer.valueOf(slotServed).shortValue());
    request.setIP(geo);
    request.setHandset(handsetMeta);
    request.setInventory(inventory);
    request.setUser(user);
    if(requestSlot != null)
      request.setSlot_requested(slotRequested);
    if(null != sasParams && null != sasParams.getSiteSegmentId())
      request.setSegmentId(sasParams.getSiteSegmentId());

    List<Impression> impressions = null;
    if(null != impression) {
      impressions = new ArrayList<Impression>();
      impressions.add(impression);
    }
    AdRR adRR = new AdRR(host, timestamp, request, impressions, isTerminated, terminationReason);
    if(enableDatabusLogging) {
      TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
      Message msg = new Message(tSerializer.serialize(adRR));
      dataBusPublisher.publish(rrLogKey, msg);
    }
  }

  // Write Channel Logs
  public static void channelLogline(List<ChannelSegment> rankList, String clickUrl, DebugLogger logger,
      Configuration config, SASRequestParameters sasParams, long totalTime) throws JSONException, TException {
    logger.debug("came inside channel log line");
    Logger debugLogger = Logger.getLogger(config.getString("channel"));
    logger.debug("got logger handle for cas logs");
    char sep = 0x01;
    StringBuilder log = new StringBuilder();
    log.append("trtt=").append(totalTime);
    InspectorStats.incrementStatCount(InspectorStrings.latency, totalTime);
    if(null != sasParams && sasParams.getSiteId() != null)
      log.append(sep + "rq-mk-siteid=\"").append(sasParams.getSiteId()).append("\"");

    String timestamp = ReportTime.getUTCTimestamp();
    log.append(sep).append("ttime=\"").append(timestamp).append("\"");
    if(null != sasParams && sasParams.getTid() != null)
      log.append(sep).append("tid=\"").append(sasParams.getTid()).append("\"");
    if(clickUrl != null)
      log.append(sep + "clurl=\"" + clickUrl + "\"");
    log.append(sep).append("rq-tpan=[");
    logger.debug("sasparams not null here");

    List<AdResponse> responseList = new ArrayList<AdResponse>();

    // Writing inspector stats and getting log line from adapters
    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      JSONObject logLine = null;
      AdNetworkInterface adNetwork = ((ChannelSegment) rankList.get(index)).getAdNetworkInterface();
      ThirdPartyAdResponse adResponse = adNetwork.getResponseStruct();
      try {
        InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalRequests);
        InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.latency, adResponse.latency);
        InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.connectionLatency, adNetwork.getConnectionLatency());
        if(adResponse.adStatus.equals("AD"))
          InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalFills);
        else if(adResponse.adStatus.equals("NO_AD"))
          InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalNoFills);
        else if(adResponse.adStatus.equals("TIME_OUT"))
          InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalTimeout);
        else
          InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalTerminate);
        logLine = new JSONObject();
        String advertiserId = adNetwork.getId();
        String externalSiteKey = ((ChannelSegment) rankList.get(index)).getChannelSegmentEntity().getExternalSiteKey();
        double bid = adNetwork.getBidprice();
        String resp = adResponse.adStatus;
        long latency = adResponse.latency;
        logLine.put("adv", advertiserId);
        logLine.put("3psiteid", externalSiteKey);
        logLine.put("resp", resp);
        logLine.put("latency", adResponse.latency);
        AdResponse response = new AdResponse(advertiserId, externalSiteKey, resp, latency);
        if(bid != -1) {
          logLine.put("bid", bid);
          response.setBid(bid);
        }
        responseList.add(response);

      } catch (JSONException exception) {
        logger.error("error reading channel log line from the adapters");
      } catch (NullPointerException exception) {
        logger.error("error reading channel log line from the adapters " + exception.getMessage());
      }

      if(logLine != null) {
        log.append(logLine);
        if(index != rankList.size() - 1)
          log.append(",");
      }
      if(index == rankList.size() - 1)
        log.append("]").append(sep);
    }
    if(rankList == null || rankList.size() == 0)
      log.append("]").append(sep);

    // Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
    // Gson gson = new Gson();
    // List<Integer> category = gson.fromJson(getCategories(jObject, logger),
    // ArrayList.class);
    ContentRating siteType = getContentRating(sasParams);

    List<Integer> categ = null;
    if(null != sasParams && sasParams.getCategories() != null) {
      categ = new ArrayList<Integer>();
      for (long cat : sasParams.getCategories()) {
        categ.add((int) cat);
      }
    }

    SiteParams siteParams = new SiteParams(categ, siteType);
    RequestParams requestParams = sasParams == null ? new RequestParams(null, null, null) : new RequestParams(
        sasParams.getRemoteHostIp(), sasParams.getSource(), sasParams.getUserAgent());

    if(null != sasParams && null != sasParams.getRemoteHostIp())
      log.append("rq-params={\"host\":\"").append(sasParams.getRemoteHostIp()).append("\"");
    JSONArray carrier = null;
    if(null != sasParams) {
      if(sasParams.getSource() != null)
        log.append(",\"src\":\"").append(sasParams.getSource()).append("\"");
      log.append("}").append(sep).append("rq-h-user-agent=\"");
      log.append(sasParams.getUserAgent()).append("\"").append(sep).append("rq-site-params=[{\"categ\":");
      log.append(sasParams.getCategories().toString()).append("},{\"type\":\"" + sasParams.getSiteType() + "\"}]");
      carrier = sasParams.getCarrier();
    }

    Geo geo = null;
    if(null != carrier) {
      log.append(sep).append("carrier=").append(carrier);
      geo = new Geo(carrier.getInt(0), Integer.valueOf(carrier.getInt(1)).shortValue());
      if(carrier.length() >= 4 && carrier.get(3) != null)
        geo.setRegion(carrier.getInt(3));
      if(carrier.length() >= 5 && carrier.get(4) != null)
        geo.setCity(carrier.getInt(4));
    }
    if(null != sasParams && null != sasParams.getSiteSegmentId())
        log.append(sep).append("sel-seg-id=").append(sasParams.getSiteSegmentId());

    logger.debug("finished writing cas logs");
    logger.debug(log.toString());
    if(enableFileLogging)
      debugLogger.info(log.toString());
    CasChannelLog channelLog = new CasChannelLog(totalTime, clickUrl, sasParams == null ? null : sasParams.getSiteId(),
        new RequestTpan(responseList), siteParams, requestParams, timestamp);
    if(null != geo)
      channelLog.setIP(geo);
    if(enableDatabusLogging) {
      TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
      Message msg = new Message(tSerializer.serialize(channelLog));
      dataBusPublisher.publish(channelLogKey, msg);
    }
  }

  public static void advertiserLogging(List<ChannelSegment> rankList, DebugLogger logger, Configuration config) {
    logger.debug("came inside advertiser log");
    Logger advertiserLogger = Logger.getLogger(config.getString("advertiser"));
    if(!advertiserLogger.isDebugEnabled())
      return;
    char sep = 0x01;
    StringBuilder log = new StringBuilder();
    logger.debug("got logger handle for advertiser logs");
    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      AdNetworkInterface adNetworkInterface = ((ChannelSegment) rankList.get(index)).getAdNetworkInterface();
      ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
      String partnerName = adNetworkInterface.getName();
      log.append(partnerName);
      log.append(sep).append(adResponse.adStatus);
      String response = "";
      String requestUrl = "";
      if(adResponse.adStatus.equalsIgnoreCase("AD")) {
        response = adNetworkInterface.getHttpResponseContent();
        log.append(sep).append(response);
      }
      if(!adNetworkInterface.getRequestUrl().equals("")) {
        requestUrl = adNetworkInterface.getRequestUrl();
        log.append(sep).append(requestUrl);
      }
      if(index != rankList.size() - 1)
        log.append("\n");
    }
    if(enableFileLogging && log.length() > 0) {
      advertiserLogger.debug(log);
      logger.debug("done with advertiser logging");
    }
  }

  public static void sampledAdvertiserLogging(List<ChannelSegment> rankList, DebugLogger logger, Configuration config) {
    logger.debug("came inside sampledAdvertiser log");
    Logger sampledAdvertiserLogger = Logger.getLogger(config.getString("sampledadvertiser"));
    if(!sampledAdvertiserLogger.isDebugEnabled())
      return;
    char sep = 0x01;
    StringBuilder log = new StringBuilder();
    logger.debug("got logger handle for sampledAdvertiser logs");

    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      AdNetworkInterface adNetworkInterface = ((ChannelSegment) rankList.get(index)).getAdNetworkInterface();
      ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
      String adstatus = adResponse.adStatus;
      if(!adstatus.equalsIgnoreCase("AD"))
        continue;
      String partnerName = adNetworkInterface.getName();
      String extsiteKey = rankList.get(index).getChannelSegmentEntity().getExternalSiteKey();
      String advertiserId = rankList.get(index).getChannelSegmentEntity().getAdvertiserId();
      String requestUrl = "";
      String response = "";
      if(sampledAdvertiserLogNos.get(partnerName + extsiteKey) == null) {
        sampledAdvertiserLogNos.put(partnerName + extsiteKey, System.currentTimeMillis() + "_" + 0);
      }
      Long time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[0]);
      Integer count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[1]);

      if(System.currentTimeMillis() - time < 3600000) {
        if(count < totalCount) {
          requestUrl = adNetworkInterface.getRequestUrl();
          response = adNetworkInterface.getHttpResponseContent();
          if(requestUrl.equals("") || response.equals(""))
            continue;
          if(index > 0 && partnerName.length() > 0 && log.length() > 0)
            log.append("\n");
          log.append(partnerName).append(sep)
              .append(rankList.get(index).getChannelSegmentEntity().getExternalSiteKey());
          log.append(sep).append(requestUrl).append(sep).append(adResponse.adStatus);
          log.append(sep).append(response).append(sep).append(advertiserId);
          count++;
          sampledAdvertiserLogNos.put(partnerName + extsiteKey, time + "_" + count);
        }
      } else {
        logger.debug("creating new sampledadvertiser logs");
        count = 0;
        sampledAdvertiserLogNos.put(partnerName + extsiteKey, System.currentTimeMillis() + "_" + 0);
        time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[0]);
        count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + extsiteKey).split("_")[1]);
        requestUrl = adNetworkInterface.getRequestUrl();
        response = adNetworkInterface.getHttpResponseContent();
        if(requestUrl.equals("") || response.equals("")) {
          continue;
        }
        if(index > 0 && partnerName.length() > 0 && log.length() > 0)
          log.append("\n");
        log.append(partnerName).append(sep).append(rankList.get(index).getChannelSegmentEntity().getExternalSiteKey());
        log.append(sep).append(requestUrl).append(sep).append(adResponse.adStatus);
        log.append(sep).append(response).append(sep).append(advertiserId);
        count++;
        sampledAdvertiserLogNos.put(partnerName + extsiteKey, time + "_" + count);
      }
      if(enableDatabusLogging) {
        if(count >= totalCount)
          continue;
        CasAdvertisementLog casAdvertisementLog = new CasAdvertisementLog(partnerName, requestUrl, response, adstatus,
            extsiteKey, advertiserId);
        Message msg = null;
        try {
          TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
          msg = new Message(tSerializer.serialize(casAdvertisementLog));
        } catch (TException e) {
          logger.debug("Error while creating sampledAdvertiser logs for databus ");
          e.printStackTrace();
        }
        if(null != msg) {
          dataBusPublisher.publish(sampledAdvertisementLogKey, msg);
        } else {
          logger.debug("In sampledAdvertiser log: log msg is null");
        }
      } else {
        logger.debug("In sampledAdvertiser log: enableDatabusLogging is false ");
      }
    }
    if(enableFileLogging && log.length() > 0) {
      sampledAdvertiserLogger.debug(log);
      logger.debug("done with sampledAdvertiser logging");
    }
  }

  public static ContentRating getContentRating(SASRequestParameters sasParams) {
    if(sasParams == null)
      return null;
    if(sasParams.getSiteType() == null)
      return null;
    if(sasParams.getSiteType().equalsIgnoreCase("performance"))
      return ContentRating.PERFORMANCE;
    if(sasParams.getSiteType().equalsIgnoreCase("FAMILY_SAFE"))
      return ContentRating.FAMILY_SAFE;
    if(sasParams.getSiteType().equalsIgnoreCase("MATURE"))
      return ContentRating.MATURE;
    else
      return null;
  }

  public static PricingModel getPricingModel(String pricingModel) {
    if(pricingModel == null)
      return null;
    if(pricingModel.equalsIgnoreCase("cpc"))
      return PricingModel.CPC;
    if(pricingModel.equalsIgnoreCase("cpm"))
      return PricingModel.CPM;
    else
      return null;
  }

  public static InventoryType getInventoryType(SASRequestParameters sasParams) {
    if(null != sasParams && sasParams.getSdkVersion() != null 
    	&& sasParams.getSdkVersion().equalsIgnoreCase("0"))
      return InventoryType.BROWSER;
    return InventoryType.APP;
  }

  public static Gender getGender(SASRequestParameters sasParams) {
    if(sasParams == null)
      return null;
    if(sasParams.getGender().equalsIgnoreCase("m"))
      return Gender.MALE;
    else
      return Gender.FEMALE;
  }
}
