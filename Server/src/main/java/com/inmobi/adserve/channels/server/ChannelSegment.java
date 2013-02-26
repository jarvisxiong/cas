package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;

public class ChannelSegment {
  private ChannelSegmentEntity channelSegmentEntity;
  private ChannelEntity channelEntity;
  private ChannelFeedbackEntity channelFeedbackEntity;
  private ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
  private ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity;
  private AdNetworkInterface adNetworkInterface;
  private double prioritisedECPM;

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

  public ChannelSegmentEntity getChannelSegmentEntity() {
    return channelSegmentEntity;
  }

  public ChannelEntity getChannelEntity() {
    return channelEntity;
  }

  public ChannelFeedbackEntity getChannelFeedbackEntity() {
    return channelFeedbackEntity;
  }

  public ChannelSegmentFeedbackEntity getChannelSegmentFeedbackEntity() {
    return channelSegmentFeedbackEntity;
  }

  public ChannelSegmentFeedbackEntity getChannelSegmentCitrusLeafFeedbackEntity() {
    return channelSegmentCitrusLeafFeedbackEntity;
  }

  public AdNetworkInterface getAdNetworkInterface() {
    return adNetworkInterface;
  }

  public double getPrioritisedECPM() {
    return prioritisedECPM;
  }

  public void setAdNetworkInterface(AdNetworkInterface adNetworkInterface) {
    this.adNetworkInterface = adNetworkInterface;
  }
  
  public void setPrioritisedECPM(double prioritisedECPM) {
    this.prioritisedECPM = prioritisedECPM;
  }

  
}
