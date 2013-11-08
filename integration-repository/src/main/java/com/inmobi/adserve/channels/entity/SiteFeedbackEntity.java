package com.inmobi.adserve.channels.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;


@Getter
@ToString
public class SiteFeedbackEntity
{

    @Setter
    private long                                                      lastUpdated;
    private final String                                              siteGuId;
    private Map<Integer/* segmentId */, SegmentAdGroupFeedbackEntity> segmentAdGroupFeedbackMap;

    public SiteFeedbackEntity(Builder builder)
    {
        this.lastUpdated = builder.lastUpdated;
        this.siteGuId = builder.siteGuId;
        this.segmentAdGroupFeedbackMap = builder.segmentAdGroupFeedbackMap;
    }

    public SiteFeedbackEntity(String siteId)
    {
        this.siteGuId = siteId;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Setter
    public static class Builder
    {
        private long                                       lastUpdated;
        private String                                     siteGuId;
        private Map<Integer, SegmentAdGroupFeedbackEntity> segmentAdGroupFeedbackMap;

        public SiteFeedbackEntity build()
        {
            return new SiteFeedbackEntity(this);
        }

    }

}
