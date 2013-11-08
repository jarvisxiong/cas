package com.inmobi.adserve.channels.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;


@Getter
@AllArgsConstructor
@ToString
public class SegmentAdGroupFeedbackEntity
{
    private final Integer                                                  segmentId;
    private final Map<String/* AdgroupId */, ChannelSegmentFeedbackEntity> adGroupFeedbackMap;
}
