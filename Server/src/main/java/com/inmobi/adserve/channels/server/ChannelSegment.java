package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;

public class ChannelSegment {
  public ChannelSegmentEntity channelSegmentEntity;
  public ChannelEntity channelEntity;
  public ChannelFeedbackEntity channelFeedbackEntity;
  public ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
  public ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity;
  public AdNetworkInterface adNetworkInterface;
  public double prioritisedECPM;

  public ChannelSegment(ChannelSegmentEntity channelSegmentEntity, ChannelEntity channelEntity,
      ChannelFeedbackEntity channelFeedbackEntity, ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity,
      ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity, AdNetworkInterface adNetworkInterface,
      double prioritisedECPM) {
    this.channelSegmentEntity = channelSegmentEntity;
    this.channelEntity = channelEntity;
    this.channelFeedbackEntity = channelFeedbackEntity;
    this.channelSegmentFeedbackEntity = channelSegmentFeedbackEntity;
    this.channelSegmentCitrusLeafFeedbackEntity = channelSegmentCitrusLeafFeedbackEntity;
    this.adNetworkInterface = adNetworkInterface;
    this.prioritisedECPM = prioritisedECPM;
  }

}
