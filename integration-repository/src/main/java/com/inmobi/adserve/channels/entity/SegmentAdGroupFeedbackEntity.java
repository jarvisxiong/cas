package com.inmobi.adserve.channels.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
public class SegmentAdGroupFeedbackEntity {
  private final Integer segmentId;
  private final Map<String/* AdgroupId */, ChannelSegmentFeedbackEntity> adGroupFeedbackMap;
}
