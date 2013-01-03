package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;

public class ChannelSegment {
  public ChannelSegmentEntity channelSegmentEntity;
  public AdNetworkInterface adNetworkInterface;
  public ChannelEntity channelEntity;
  public ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
  public double lowerPriorityRange;
  public double higherPriorityRange;

  public ChannelSegment(ChannelSegmentEntity channelSegmentEntity, AdNetworkInterface adNetworkInterface, ChannelEntity channelEntity,
      ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity) {
    this.channelSegmentEntity = channelSegmentEntity;
    this.adNetworkInterface = adNetworkInterface;
    this.channelEntity = channelEntity;
    this.channelSegmentFeedbackEntity = channelSegmentFeedbackEntity;
  }
}
