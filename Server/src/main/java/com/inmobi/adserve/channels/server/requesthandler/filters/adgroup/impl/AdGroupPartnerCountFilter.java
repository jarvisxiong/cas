package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.hadoop.thirdparty.guava.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.constants.ChannelSegmentFilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupPartnerCountFilter implements AdGroupLevelFilter {
    private static final Logger              LOG = LoggerFactory.getLogger(AdGroupPartnerCountFilter.class);
    private final Map<String, AdapterConfig> advertiserIdConfigMap;
    private final Provider<Marker>           traceMarkerProvider;
    private ChannelSegmentFilterOrder        order;

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupPartnerCountFilter(final Provider<Marker> traceMarkerProvider,
            final Map<String, AdapterConfig> advertiserIdConfigMap) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    @Override
    public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
            final CasContext casContext) {

        Marker traceMarker = traceMarkerProvider.get();

        Map<String, List<ChannelSegment>> advertiserSegmentListMap = getAdvertiserSegmentListMap(channelSegments);

        for (Entry<String, List<ChannelSegment>> entry : advertiserSegmentListMap.entrySet()) {
            List<ChannelSegment> segmentListForAdvertiser = entry.getValue();
            String advertiserId = entry.getKey();
            Collections.sort(segmentListForAdvertiser, ChannelSegment.CHANNEL_SEGMENT_REVERSE_COMPARATOR);

            List<ChannelSegment> selectedSegmentListForAdvertiser = Lists.newArrayList();
            advertiserSegmentListMap.put(advertiserId, selectedSegmentListForAdvertiser);

            for (ChannelSegment channelSegment : segmentListForAdvertiser) {

                AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserId);

                boolean result = failedInFilter(selectedSegmentListForAdvertiser.size(), adapterConfig);

                if (result) {
                    LOG.debug(traceMarker, "Failed in filter {}  , advertiser {}", this.getClass().getSimpleName(),
                            advertiserId);
                    break;
                }
                else {
                    selectedSegmentListForAdvertiser.add(channelSegment);
                    LOG.debug(traceMarker, "Passed in filter {} ,  advertiser {}", this.getClass().getSimpleName(),
                            advertiserId);
                    incrementStats(channelSegment);
                }
            }
        }

        channelSegments.clear();
        for (List<ChannelSegment> channelSegmentList : advertiserSegmentListMap.values()) {
            channelSegments.addAll(channelSegmentList);
        }

    }

    /**
     * @param channelSegments
     */
    private Map<String, List<ChannelSegment>> getAdvertiserSegmentListMap(final List<ChannelSegment> channelSegments) {
        Map<String, List<ChannelSegment>> advertiserSegmentListMap = Maps.newHashMap();

        for (ChannelSegment channelSegment : channelSegments) {
            String advertiserId = channelSegment.getChannelEntity().getAccountId();

            List<ChannelSegment> segmentListForAdvertiser = advertiserSegmentListMap.get(advertiserId);
            if (segmentListForAdvertiser == null) {
                segmentListForAdvertiser = Lists.newArrayList();
                advertiserSegmentListMap.put(advertiserId, segmentListForAdvertiser);
            }
            segmentListForAdvertiser.add(channelSegment);
        }
        return advertiserSegmentListMap;
    }

    /**
     * @param segmentCountForAdvertiser
     * @param adapterConfig
     * @return boolean
     */
    private boolean failedInFilter(final int segmentCountForAdvertiser, final AdapterConfig adapterConfig) {
        return segmentCountForAdvertiser >= adapterConfig.getMaxSegmentSelectionCount();
    }

    /**
     * @param channelSegment
     */
    private void incrementStats(final ChannelSegment channelSegment) {
        channelSegment.incrementInspectorStats(InspectorStrings.totalSelectedSegments);
    }

    @Override
    final public void setOrder(final ChannelSegmentFilterOrder order) {
        this.order = order;
    }

    @Override
    public ChannelSegmentFilterOrder getOrder() {
        return order;
    }

}
