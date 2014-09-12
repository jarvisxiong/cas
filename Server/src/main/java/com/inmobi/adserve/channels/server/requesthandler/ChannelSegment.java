package com.inmobi.adserve.channels.server.requesthandler;

import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;


public class ChannelSegment {
    @Getter
    private ChannelSegmentEntity                   channelSegmentEntity;
    @Getter
    private final ChannelEntity                    channelEntity;
    @Getter
    private final ChannelFeedbackEntity            channelFeedbackEntity;
    @Getter
    private final ChannelSegmentFeedbackEntity     channelSegmentFeedbackEntity;
    @Getter
    private final ChannelSegmentFeedbackEntity     channelSegmentCitrusLeafFeedbackEntity;
    @Getter
    @Setter
    private AdNetworkInterface                     adNetworkInterface;
    @Getter
    @Setter
    private double                                 prioritisedECPM;

    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String>             advertiserIdNameMap;

    public final static Comparator<ChannelSegment> CHANNEL_SEGMENT_REVERSE_COMPARATOR = new Comparator<ChannelSegment>() {
                                                                                          @Override
                                                                                          public int compare(
                                                                                                  final ChannelSegment o1,
                                                                                                  final ChannelSegment o2) {
                                                                                              return o1
                                                                                                      .getPrioritisedECPM() > o2
                                                                                                      .getPrioritisedECPM() ? -1
                                                                                                      : 1;
                                                                                          }
                                                                                      };

    public ChannelSegment(final ChannelSegmentEntity channelSegmentEntity, final ChannelEntity channelEntity,
            final ChannelFeedbackEntity channelFeedbackEntity,
            final ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity,
            final ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity,
            final AdNetworkInterface adNetworkInterface, final double prioritisedECPM) {
        this.channelSegmentEntity = channelSegmentEntity;
        this.channelEntity = channelEntity;
        this.channelFeedbackEntity = channelFeedbackEntity;
        this.channelSegmentFeedbackEntity = channelSegmentFeedbackEntity;
        this.channelSegmentCitrusLeafFeedbackEntity = channelSegmentCitrusLeafFeedbackEntity;
        this.adNetworkInterface = adNetworkInterface;
        this.prioritisedECPM = prioritisedECPM;
    }

    public void incrementInspectorStats(final String inspectorString) {
        incrementInspectorStats(inspectorString, 1L);
    }

    public void incrementInspectorStats(final String inspectorString, long value) {
        String advertiserId = channelSegmentEntity.getAdvertiserId();
        if (advertiserIdNameMap.containsKey(advertiserId)) {
            InspectorStats.incrementStatCount(advertiserIdNameMap.get(advertiserId), inspectorString, value);
        }
    }

}
