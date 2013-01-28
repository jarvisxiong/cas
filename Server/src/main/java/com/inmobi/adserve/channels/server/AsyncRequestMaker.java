package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.hadoop.hdfs.server.namenode.status_jsp;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ClickUrlMaker.TrackingUrls;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;

public class AsyncRequestMaker {

  private static ClientBootstrap clientBootstrap;
  private static ClientBootstrap rtbClientBootstrap;

  public static void init(ClientBootstrap clientBootstrap, ClientBootstrap rtbClientBootstrap) {
    AsyncRequestMaker.clientBootstrap = clientBootstrap;
    AsyncRequestMaker.rtbClientBootstrap = rtbClientBootstrap;
  }

  /**
   * For each channel we configure the parameters and make the async request if
   * the async request is successful we add it to segment list else we drop it
   */
  public static List<ChannelSegment> prepareForAsyncRequest(ChannelSegmentEntity[] rows, DebugLogger logger,
      Configuration config, Configuration rtbConfig, Configuration adapterConfig, HttpRequestHandlerBase base,
      Set<String> advertiserSet, MessageEvent e, RepositoryHelper repositoryHelper, JSONObject jObject,
      SASRequestParameters sasParams, CasInternalRequestParameters casInternalRequestParameters) throws Exception {

    List<ChannelSegment> segments = new ArrayList<ChannelSegment>();
    List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

    logger.debug("Total channels available for sending requests", rows.length + "");
   
    for (ChannelSegmentEntity row : rows) {
      boolean isRtbEnabled = false;
      isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled", false);
      logger.debug("isRtbEnabled is " + isRtbEnabled);

      AdNetworkInterface network = SegmentFactory.getChannel(row.getId(), row.getChannelId(), adapterConfig,
          clientBootstrap, rtbClientBootstrap, base, e, advertiserSet, logger, isRtbEnabled, casInternalRequestParameters);
      if(null == network) {
        logger.debug("No adapter found for adGroup:", row.getAdgroupId());
        continue;
      }
      logger.debug("adapter found for adGroup:", row.getAdgroupId(), "advertiserid is", row.getId());
      if(null == repositoryHelper.queryChannelRepository(row.getChannelId())) {
        logger.debug("No channel entity found for channel id:", row.getChannelId());
        continue;
      }

      InspectorStats.initializeNetworkStats(network.getName());

      String clickUrl = null;
      String beaconUrl = null;
      sasParams.impressionId = getImpressionId(row.getIncId());
      sasParams.adIncId = row.getIncId();
      logger.debug("impression id is " + sasParams.impressionId);

      if((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.impressionId) {
        if(config.getInt("clickmaker.version", 6) == 4) {
          ClickUrlMaker clickUrlMaker = new ClickUrlMaker(config, jObject, sasParams, logger);
          TrackingUrls trackingUrls = clickUrlMaker.getClickUrl(row.getPricingModel());
          clickUrl = trackingUrls.getClickUrl();
          beaconUrl = trackingUrls.getBeaconUrl();
          if(logger.isDebugEnabled()) {
            logger.debug("click url formed is", clickUrl);
            logger.debug("beacon url :", beaconUrl);
          }
        } else {
          boolean isCpc = false;
          if(null != row.getPricingModel() && row.getPricingModel().equalsIgnoreCase("cpc"))
            isCpc = true;
          ClickUrlMakerV6 clickUrlMakerV6 = setClickParams(logger, isCpc, config, sasParams, jObject);
          Map<String, String> clickGetParams = new HashMap<String, String>();
          clickGetParams.put("ds", "1");
          Map<String, String> beaconGetParams = new HashMap<String, String>();
          beaconGetParams.put("ds", "1");
          beaconGetParams.put("event", "beacon");
          clickUrlMakerV6.createClickUrls();
          clickUrl = clickUrlMakerV6.getClickUrl(clickGetParams);
          beaconUrl = clickUrlMakerV6.getBeaconUrl(beaconGetParams);
          if(logger.isDebugEnabled()) {
            logger.debug("click url formed is " + clickUrl);
            logger.debug("beacon url : " + beaconUrl);
          }
        }
      }

      logger.debug("Sending request to Channel of Id", row.getId());
      logger.debug("external site key is", row.getExternalSiteKey());

      if(network.configureParameters(sasParams, row, clickUrl, beaconUrl)) {
        InspectorStats.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper
            .queryChannelSegmentFeedbackRepository(row.getAdgroupId());
        if(null == channelSegmentFeedbackEntity)
          channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(row.getId(), row.getAdgroupId(),
              config.getDouble("default.ecpm"), config.getDouble("default.fillratio"));
        ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(row.getChannelId());
        if(channelEntity != null) {
          if(network.isRtbPartner()) {
            rtbSegments.add(new ChannelSegment(row, network, channelEntity, channelSegmentFeedbackEntity));
            logger.debug(network.getName(), "is a rtb partner so adding this network to rtb ranklist");
          } else {
            segments.add(new ChannelSegment(row, network, channelEntity, channelSegmentFeedbackEntity));
          }
        }
      }
    }
    return segments;
  }

  public static List<ChannelSegment> makeAsyncRequests(List<ChannelSegment> rankList, DebugLogger logger,
      HttpRequestHandlerBase base, MessageEvent e) {
    Iterator<ChannelSegment> itr = rankList.iterator();
    while (itr.hasNext()) {
      ChannelSegment channelSegment = itr.next();
      InspectorStats.incrementStatCount(channelSegment.adNetworkInterface.getName(), InspectorStrings.totalInvocations);
      if(channelSegment.adNetworkInterface.makeAsyncRequest()) {
        if(logger.isDebugEnabled())
          logger.debug("Successfully sent request to channel of  advertiser id",
              channelSegment.channelSegmentEntity.getId(), "and channel id",
              channelSegment.channelSegmentEntity.getChannelId());
      } else {
        itr.remove();
      }
    }
    logger.debug("Number of tpans whose request was successfully completed", rankList.size() + "");
    return rankList;
  }

  public static String getImpressionId(long adId) {
    String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
    String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, ChannelServer.hostIdCode)).toString();
    return (WilburyUUID.setDataCenterId(uuidMachineKey, ChannelServer.dataCenterIdCode)).toString();
  }

  private static ClickUrlMakerV6 setClickParams(DebugLogger logger, boolean pricingModel, Configuration config,
      SASRequestParameters sasParams, JSONObject jObject) {
    Set<String> unhashable = new HashSet<String>();
    unhashable.addAll(Arrays.asList(config.getStringArray("clickmaker.unhashable")));
    ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(logger, unhashable);
    if(null != sasParams.age)
      clickUrlMakerV6.setAge(Integer.parseInt(sasParams.age));
    if(null != sasParams.gender)
      clickUrlMakerV6.setGender(sasParams.gender);
    clickUrlMakerV6.setCPC(pricingModel);
    Integer carrierId = null;
    try {
      carrierId = jObject.getJSONArray("carrier").getInt(0);
    } catch (JSONException e) {
      logger.error("CarrierId is not present in the sasParams");
    }
    if(null != carrierId)
      clickUrlMakerV6.setCarrierId(carrierId);
    if(null != sasParams.countryStr) {
      clickUrlMakerV6.setCountryId(Integer.parseInt(sasParams.countryStr));
    }
    try {
      if(null != jObject.getJSONArray("handset"))
        try {
          clickUrlMakerV6.setHandsetInternalId(Long.parseLong(jObject.getJSONArray("handset").get(0).toString()));
        } catch (NumberFormatException e) {
          logger.error("NumberFormatException while parsing handset");
        } catch (JSONException e) {
          logger.error("CountryId is not present in the sasParams");
        }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    if(null == sasParams.impressionId) {
      logger.debug("impression id is null");
    } else {
      clickUrlMakerV6.setImpressionId(sasParams.impressionId);
    }
    clickUrlMakerV6.setIpFileVersion(Long.parseLong(config.getString("clickmaker.ipFileVersion")));
    clickUrlMakerV6.setIsBillableDemog(false);
    if(null != sasParams.area)
      clickUrlMakerV6.setLocation(Integer.parseInt(sasParams.area));
    if(null != sasParams.siteSegmentId)
      clickUrlMakerV6.setSegmentId(sasParams.siteSegmentId);
    clickUrlMakerV6.setSiteIncId(sasParams.siteIncId);
    Map<String, String> uidMap = new HashMap<String, String>();
    JSONObject userIdMap = null;
    try {
      userIdMap = (JSONObject) jObject.get("u-id-params");
    } catch (JSONException e) {
      logger.debug("u-id-params is not present in the request");
    }
    if(null == userIdMap && null != sasParams.uid)
      uidMap.put("U-ID", sasParams.uid);
    else {
      Iterator userMapIterator = userIdMap.keys();
      while(userMapIterator.hasNext()) {
        String key = (String)userMapIterator.next();
        String value = null;
        try {
          value = (String) userIdMap.get(key);
        } catch (JSONException e) {
          logger.debug("value corresponding to uid key is not present in the uidMap");
        }
        if(null != value)
          uidMap.put(key.toUpperCase(), value);
      }
    }

    clickUrlMakerV6.setUdIdVal(uidMap);
    clickUrlMakerV6.setCryptoKeyType(config.getString("clickmaker.key.1.type"));
    clickUrlMakerV6.setTestCryptoKeyType(config.getString("clickmaker.key.2.type"));
    clickUrlMakerV6.setCryptoSecretKey(config.getString("clickmaker.key.1.value"));
    clickUrlMakerV6.setTestCryptoSecretKey(config.getString("clickmaker.key.2.value"));
    clickUrlMakerV6.setImageBeaconFlag(true);// true/false
    clickUrlMakerV6.setBeaconEnabledOnSite(true);// do not know
    clickUrlMakerV6.setTestMode(false);
    clickUrlMakerV6.setRmAd(jObject.optBoolean("rich-media", false));
    clickUrlMakerV6.setRmBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
    clickUrlMakerV6.setClickURLPrefix(config.getString("clickmaker.clickURLPrefix"));
    clickUrlMakerV6.setImageBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
    return clickUrlMakerV6;
  }
}