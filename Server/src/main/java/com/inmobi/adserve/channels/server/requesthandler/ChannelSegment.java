package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
public class ChannelSegment {
    private ChannelSegmentEntity         channelSegmentEntity;
    private ChannelEntity                channelEntity;
    private ChannelFeedbackEntity        channelFeedbackEntity;
    private ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
    private ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity;
    @Setter
    private AdNetworkInterface           adNetworkInterface;
    @Setter
    private double                       prioritisedECPM;

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

    public void incrementInspectorStats(String inspectorString) {
        Map<String, String> advertiserIdToNameMapping = Filters.getAdvertiserIdToNameMapping();
        String advertiserId = channelSegmentEntity.getAdvertiserId();
        if (advertiserIdToNameMapping.containsKey(advertiserId)) {
            InspectorStats.incrementStatCount(advertiserIdToNameMapping.get(advertiserId), inspectorString);
        }
    }
}
