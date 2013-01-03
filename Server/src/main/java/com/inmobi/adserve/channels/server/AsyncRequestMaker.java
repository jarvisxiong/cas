package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ClickUrlMaker.TrackingUrls;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;

public class AsyncRequestMaker {

  /**
   * For each channel we configure the parameters and make the async request if
   * the async request is successful we add it to segment list else we drop it
   */
  public static List<ChannelSegment> prepareForAsyncRequest(ChannelSegmentEntity[] rows, DebugLogger logger,
      Configuration config, Configuration rtbConfig, Configuration adapterConfig, ClientBootstrap clientBootstrap,
      ClientBootstrap rtbClientBootstrap, HttpRequestHandlerBase base, Set<String> advertiserSet, MessageEvent e,
      RepositoryHelper repositoryHelper, JSONObject jObject, SASRequestParameters sasParams) throws Exception {

    List<ChannelSegment> segments = new ArrayList<ChannelSegment>();
    List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();

    logger.debug("Total channels available for sending requests", rows.length + "");

    for (ChannelSegmentEntity row : rows) {
      boolean isRtbEnabled = false;
      isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled", false);
      logger.debug("isRtbEnabled is " + isRtbEnabled);

      AdNetworkInterface network = SegmentFactory.getChannel(row.getId(), row.getChannelId(), adapterConfig, clientBootstrap,
          rtbClientBootstrap, base, e, advertiserSet, logger, isRtbEnabled);
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
      sasParams.segmentCategories = row.getTags();
      logger.debug("impression id is " + sasParams.impressionId);

      if((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.impressionId) {
        ClickUrlMaker clickUrlMaker = new ClickUrlMaker(config, jObject, sasParams, logger);
        TrackingUrls trackingUrls = clickUrlMaker.getClickUrl(row.getPricingModel());
        clickUrl = trackingUrls.getClickUrl();
        beaconUrl = trackingUrls.getBeaconUrl();
        logger.debug("click url formed is", clickUrl);
        logger.debug("beacon url :", beaconUrl);
      }

      logger.debug("Sending request to Channel of Id", row.getId());
      logger.debug("external site key is", row.getExternalSiteKey());

      if(network.configureParameters(sasParams, row.getExternalSiteKey(), clickUrl, beaconUrl)) {
        InspectorStats.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper.queryChannelSegmentFeedbackRepository(row
            .getAdgroupId());
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
          logger.debug("Successfully sent request to channel of  advertiser id", channelSegment.channelSegmentEntity.getId(),
              "and channel id", channelSegment.channelSegmentEntity.getChannelId());
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
}