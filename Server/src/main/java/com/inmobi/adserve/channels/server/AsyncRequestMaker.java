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
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ClickUrlMaker.TrackingUrls;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;
import com.ning.http.client.AsyncHttpClient;

public class AsyncRequestMaker {

  private static ClientBootstrap clientBootstrap;
  private static ClientBootstrap rtbClientBootstrap;
  private static AsyncHttpClient asyncHttpClient;

  public static AsyncHttpClient getAsyncHttpClient() {
    return asyncHttpClient;
  }

  public static void init(ClientBootstrap clientBootstrap, ClientBootstrap rtbClientBootstrap,
      AsyncHttpClient asyncHttpClient) {
    AsyncRequestMaker.clientBootstrap = clientBootstrap;
    AsyncRequestMaker.rtbClientBootstrap = rtbClientBootstrap;
    AsyncRequestMaker.asyncHttpClient = asyncHttpClient;
  }

  /**
   * For each channel we configure the parameters and make the async request if
   * the async request is successful we add it to segment list else we drop it
   */
  public static List<ChannelSegment> prepareForAsyncRequest(List<ChannelSegment> rows, DebugLogger logger,
      Configuration config, Configuration rtbConfig, Configuration adapterConfig, HttpRequestHandlerBase base,
      Set<String> advertiserSet, MessageEvent e, RepositoryHelper repositoryHelper, JSONObject jObject,
      SASRequestParameters sasParams, CasInternalRequestParameters casInternalRequestParams,
      List<ChannelSegment> rtbSegments) throws Exception {

    List<ChannelSegment> segments = new ArrayList<ChannelSegment>();

    logger.debug("Total channels available for sending requests", rows.size() + "");
    boolean isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled", false);
    logger.debug("isRtbEnabled is", new Boolean(isRtbEnabled).toString());

    for (ChannelSegment row : rows) {
      ChannelSegmentEntity channelSegmentEntity = row.getChannelSegmentEntity();
      AdNetworkInterface network = SegmentFactory.getChannel(channelSegmentEntity.getAdvertiserId(), row
          .getChannelSegmentEntity().getChannelId(), adapterConfig, clientBootstrap, rtbClientBootstrap, base, e,
          advertiserSet, logger, isRtbEnabled);
      if(null == network) {
        logger.debug("No adapter found for adGroup:", channelSegmentEntity.getAdgroupId());
        continue;
      }
      logger.debug("adapter found for adGroup:", channelSegmentEntity.getAdgroupId(), "advertiserid is", row
          .getChannelSegmentEntity().getAdvertiserId());
      if(null == repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId())) {
        logger.debug("No channel entity found for channel id:", channelSegmentEntity.getChannelId());
        continue;
      }

      InspectorStats.initializeNetworkStats(network.getName());

      String clickUrl = null;
      String beaconUrl = null;
      sasParams.impressionId = getImpressionId(channelSegmentEntity.getIncId());
      CasInternalRequestParameters casInternalRequestParameters = getCasInternalRequestParameters(sasParams,
          casInternalRequestParams);
      controlEnrichment(casInternalRequestParameters, channelSegmentEntity);
      sasParams.adIncId = channelSegmentEntity.getIncId();
      logger.debug("impression id is " + sasParams.impressionId);

      if((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.impressionId) {
        if(config.getInt("clickmaker.version", 6) == 4) {
          ClickUrlMaker clickUrlMaker = new ClickUrlMaker(config, jObject, sasParams, logger);
          TrackingUrls trackingUrls = clickUrlMaker.getClickUrl(channelSegmentEntity.getPricingModel());
          clickUrl = trackingUrls.getClickUrl();
          beaconUrl = trackingUrls.getBeaconUrl();
          if(logger.isDebugEnabled()) {
            logger.debug("click url formed is", clickUrl);
            logger.debug("beacon url :", beaconUrl);
          }
        } else {
          boolean isCpc = false;
          if(null != channelSegmentEntity.getPricingModel()
              && channelSegmentEntity.getPricingModel().equalsIgnoreCase("cpc"))
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

      logger.debug("Sending request to Channel of Id", channelSegmentEntity.getChannelId());
      logger.debug("external site key is", channelSegmentEntity.getExternalSiteKey());

      if(network
          .configureParameters(sasParams, casInternalRequestParameters, channelSegmentEntity, clickUrl, beaconUrl)) {
        InspectorStats.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
        row.setAdNetworkInterface(network);
        if(network.isRtbPartner()) {
          rtbSegments.add(row);
          logger.debug(network.getName(), "is a rtb partner so adding this network to rtb ranklist");
        } else {
          segments.add(row);
        }
      }
    }
    return segments;
  }

  private static CasInternalRequestParameters getCasInternalRequestParameters(SASRequestParameters sasParams,
      CasInternalRequestParameters casInternalRequestParams) {
    CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    casInternalRequestParameters.blockedCategories = casInternalRequestParams.blockedCategories;
    casInternalRequestParameters.highestEcpm = casInternalRequestParameters.highestEcpm;
    casInternalRequestParameters.rtbBidFloor = casInternalRequestParams.rtbBidFloor;
    casInternalRequestParameters.uidParams = sasParams.uidParams;
    casInternalRequestParameters.uid = sasParams.uid;
    casInternalRequestParameters.uidO1 = sasParams.uidO1;
    casInternalRequestParameters.uidMd5 = sasParams.uidMd5;
    casInternalRequestParameters.uidIFA = sasParams.uidIFA;
    casInternalRequestParameters.zipCode = sasParams.postalCode;
    casInternalRequestParameters.latLong = sasParams.latLong;
    return casInternalRequestParameters;
  }

  private static void controlEnrichment(CasInternalRequestParameters casInternalRequestParameters,
      ChannelSegmentEntity channelSegmentEntity) {
    casInternalRequestParameters.impressionId = getImpressionId(channelSegmentEntity.getIncId());
    if(channelSegmentEntity.isStripUdId()) {
      casInternalRequestParameters.uidParams = null;
      casInternalRequestParameters.uid = null;
      casInternalRequestParameters.uidO1 = null;
      casInternalRequestParameters.uidMd5 = null;
      casInternalRequestParameters.uidIFA = null;
    }
    if(channelSegmentEntity.isStripLatlong()) {
      casInternalRequestParameters.zipCode = null;
    }
    if(channelSegmentEntity.isStripLatlong()) {
      casInternalRequestParameters.latLong = null;
    }
    if(!channelSegmentEntity.isAppUrlEnabled()) {
      casInternalRequestParameters.appUrl = null;
    }

  }

  public static List<ChannelSegment> makeAsyncRequests(List<ChannelSegment> rankList, DebugLogger logger,
      HttpRequestHandlerBase base, MessageEvent e, List<ChannelSegment> rtbSegments) {
    Iterator<ChannelSegment> itr = rankList.iterator();
    while (itr.hasNext()) {
      ChannelSegment channelSegment = itr.next();
      InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
          InspectorStrings.totalInvocations);
      if(channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
        if(logger.isDebugEnabled())
          logger.debug("Successfully sent request to channel of  advertiser id", channelSegment
              .getChannelSegmentEntity().getId(), "and channel id", channelSegment.getChannelSegmentEntity()
              .getChannelId());
      } else {
        itr.remove();
      }
    }
    Iterator<ChannelSegment> rtbItr = rtbSegments.iterator();
    while (rtbItr.hasNext()) {
      ChannelSegment channelSegment = rtbItr.next();
      InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
          InspectorStrings.totalInvocations);
      if(channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
        if(logger.isDebugEnabled())
          logger.debug("Successfully sent request to rtb channel of  advertiser id", channelSegment
              .getChannelSegmentEntity().getId(), "and channel id", channelSegment.getChannelSegmentEntity()
              .getChannelId());
      } else {
        rtbItr.remove();
      }
    }
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
    clickUrlMakerV6.setIpFileVersion(sasParams.ipFileVersion.longValue());
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
      while (userMapIterator.hasNext()) {
        String key = (String) userMapIterator.next();
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