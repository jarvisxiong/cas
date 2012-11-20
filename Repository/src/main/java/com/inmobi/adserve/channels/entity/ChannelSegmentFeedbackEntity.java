package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

public class ChannelSegmentFeedbackEntity implements IdentifiableEntity<String> {

  private static final long serialVersionUID = 1L;

  private String advertiserId;

  private String adGroupId;

  private double eCPM;

  private double fillRatio;

  private double prioritisedECPM;

  public double getPrioritisedECPM() {
    return prioritisedECPM;
  }

  public void setPrioritisedECPM(double prioritisedECPM) {
    this.prioritisedECPM = prioritisedECPM;
  }

  public ChannelSegmentFeedbackEntity(String advertiserId, String adGroupId, double eCPM, double fillRatio) {
    this.advertiserId = advertiserId;
    this.adGroupId = adGroupId;
    this.eCPM = eCPM;
    this.fillRatio = fillRatio;
    this.prioritisedECPM = eCPM;
  }

  @Override
  public String getId() {
    return adGroupId;
  }

  public String getAdvertiserId() {
    return advertiserId;
  }

  @Override
  public String getJSON() {
    return null;
  }

  public double geteCPM() {
    return eCPM;
  }

  public double getFillRatio() {
    return fillRatio;
  }
}
