package com.inmobi.adserve.channels.server.requesthandler;

import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;

import lombok.Getter;
import lombok.Setter;


public class ChannelSegment {

    public final static Comparator<ChannelSegment> CHANNEL_SEGMENT_REVERSE_COMPARATOR =
            (o1, o2) -> o1.getPrioritisedECPM() > o2.getPrioritisedECPM() ? -1 : 1;

    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String> advertiserIdNameMap;

    @Getter
    private final ChannelSegmentEntity channelSegmentEntity;
    @Getter
    private final ChannelEntity channelEntity;
    @Getter
    private final ChannelFeedbackEntity channelFeedbackEntity;
    @Getter
    private final ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
    @Getter
    private final ChannelSegmentFeedbackEntity channelSegmentAerospikeFeedbackEntity;
    @Getter
    @Setter
    private AdNetworkInterface adNetworkInterface;
    @Getter
    @Setter
    private double prioritisedECPM;

    @Getter
    @Setter
    private Long requestedSlotId;

    public ChannelSegment(final ChannelSegmentEntity channelSegmentEntity, final ChannelEntity channelEntity,
            final ChannelFeedbackEntity channelFeedbackEntity,
            final ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity,
            final ChannelSegmentFeedbackEntity channelSegmentAerospikeFeedbackEntity,
            final AdNetworkInterface adNetworkInterface, final double prioritisedECPM) {
        this.channelSegmentEntity = channelSegmentEntity;
        this.channelEntity = channelEntity;
        this.channelFeedbackEntity = channelFeedbackEntity;
        this.channelSegmentFeedbackEntity = channelSegmentFeedbackEntity;
        this.channelSegmentAerospikeFeedbackEntity = channelSegmentAerospikeFeedbackEntity;
        this.adNetworkInterface = adNetworkInterface;
        this.prioritisedECPM = prioritisedECPM;
    }

    public void incrementInspectorStats(final String inspectorString) {
        incrementInspectorStats(inspectorString, 1L);
    }

    public void incrementInspectorStats(final String inspectorString, final long value) {
        final String advertiserId = channelSegmentEntity.getAdvertiserId();
        if (advertiserIdNameMap.containsKey(advertiserId)) {
            InspectorStats.incrementStatCount(advertiserIdNameMap.get(advertiserId), inspectorString, value);
        }
    }

    public static void incrementInspectorStats(final String advertiserId, final String inspectorString, long value) {
        if (advertiserIdNameMap.containsKey(advertiserId)) {
            InspectorStats.incrementStatCount(advertiserIdNameMap.get(advertiserId), inspectorString, value);
        }
    }

}
