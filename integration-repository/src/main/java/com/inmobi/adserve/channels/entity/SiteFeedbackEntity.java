package com.inmobi.adserve.channels.entity;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@ToString
public class SiteFeedbackEntity {

  @Setter
  private long lastUpdated;
  private final String siteGuId;
  private Map<Integer/* segmentId */, SegmentAdGroupFeedbackEntity> segmentAdGroupFeedbackMap;

  public SiteFeedbackEntity(final Builder builder) {
    lastUpdated = builder.lastUpdated;
    siteGuId = builder.siteGuId;
    segmentAdGroupFeedbackMap = builder.segmentAdGroupFeedbackMap;
  }

  public SiteFeedbackEntity(final String siteId) {
    siteGuId = siteId;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Setter
  public static class Builder {
    private long lastUpdated;
    private String siteGuId;
    private Map<Integer, SegmentAdGroupFeedbackEntity> segmentAdGroupFeedbackMap;

    public SiteFeedbackEntity build() {
      return new SiteFeedbackEntity(this);
    }

  }

}
