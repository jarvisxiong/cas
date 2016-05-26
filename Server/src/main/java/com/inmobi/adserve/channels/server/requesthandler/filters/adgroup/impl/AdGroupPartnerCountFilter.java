package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;


/**
 * @author abhishek.parwal
 */
@Singleton
public class AdGroupPartnerCountFilter implements AdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupPartnerCountFilter.class);
    private final Map<String, AdapterConfig> advertiserIdConfigMap;
    private final Provider<Marker> traceMarkerProvider;
    private FilterOrder order;

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupPartnerCountFilter(final Provider<Marker> traceMarkerProvider,
            final Map<String, AdapterConfig> advertiserIdConfigMap) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    // TODO: Refactor
    @Override
    public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
            final CasContext casContext) {
        final Marker traceMarker = traceMarkerProvider.get();
        final Map<String, List<ChannelSegment>> advertiserSegmentListMap = getAdvertiserSegmentListMap(channelSegments);
        for (final Entry<String, List<ChannelSegment>> entry : advertiserSegmentListMap.entrySet()) {
            final List<ChannelSegment> segmentListForAdvertiser = entry.getValue();
            final String advertiserId = entry.getKey();
            final Integer maxSegmentSelectionCount =
                    advertiserIdConfigMap.get(advertiserId).getMaxSegmentSelectionCount();
            // Sort on base of ecpm
            Collections.sort(segmentListForAdvertiser, ChannelSegment.CHANNEL_SEGMENT_REVERSE_COMPARATOR);

            final List<ChannelSegment> selectedSegmentListForAdvertiser = Lists.newArrayList();
            advertiserSegmentListMap.put(advertiserId, selectedSegmentListForAdvertiser);
            final Map<ChannelSegment, List<Long>> channelSegmentSlotIdMap = new HashMap<ChannelSegment, List<Long>>();

            for (final Iterator<ChannelSegment> iterator = segmentListForAdvertiser.listIterator(); iterator.hasNext();) {
                final ChannelSegment channelSegment = iterator.next();
                channelSegmentSlotIdMap.put(channelSegment,
                        Arrays.asList(channelSegment.getChannelSegmentEntity().getSlotIds()));
            }
            // Choose ChannelSegments based on requested slot order
            final int slotListSize = sasParams.getProcessedMkSlot().size();
            for (int i = 0; i < slotListSize; i++) {
                final Long slotIdFromUmp = Long.valueOf(sasParams.getProcessedMkSlot().get(i));
                for (final Iterator<ChannelSegment> iterator = segmentListForAdvertiser.listIterator(); iterator
                        .hasNext();) {
                    final ChannelSegment channelSegment = iterator.next();
                    if (channelSegmentSlotIdMap.get(channelSegment).contains(slotIdFromUmp)) {
                        final boolean result = failedInFilter(selectedSegmentListForAdvertiser, channelSegment
                            .getChannelSegmentEntity(), maxSegmentSelectionCount);
                        if (!result) {
                            addChannelSegment(selectedSegmentListForAdvertiser, maxSegmentSelectionCount,
                                    channelSegment, iterator, advertiserId, traceMarker, slotIdFromUmp);
                        }
                    }
                }
            }
            LOG.debug(traceMarker, "Number of segments {} failed in filter {}, advertiser {}",
                    segmentListForAdvertiser.size(), this.getClass().getSimpleName(), advertiserId);
            // These are the Channel Segments that could not pass the filter and hence have not been removed from
            // Channel Segment
            // We are interested in capturing these stats
            incrementStats(advertiserId, segmentListForAdvertiser.size());
        }

        channelSegments.clear();
        for (final List<ChannelSegment> channelSegmentList : advertiserSegmentListMap.values()) {
            channelSegments.addAll(channelSegmentList);
        }
    }

    private void addChannelSegment(List<ChannelSegment> selectedSegmentListForAdvertiser, int maxSegmentSelectionCount,
            ChannelSegment channelSegment, Iterator<ChannelSegment> iterator, Object advertiserId, Marker traceMarker,
            Long slotInSegment) {
        channelSegment.setRequestedSlotId(slotInSegment);
        selectedSegmentListForAdvertiser.add(channelSegment);
        LOG.debug(traceMarker, "Passed in filter {} ,  advertiser {}", this.getClass().getSimpleName(), advertiserId);
        // Removing channel segment since it has already been passed and stored in
        // selectedSegmentListForAdvertiser.
        // We don't want to traverse over it again
        iterator.remove();
    }

    /**
     * @param channelSegments
     */
    private Map<String, List<ChannelSegment>> getAdvertiserSegmentListMap(final List<ChannelSegment> channelSegments) {
        final Map<String, List<ChannelSegment>> advertiserSegmentListMap = Maps.newHashMap();
        for (final ChannelSegment channelSegment : channelSegments) {
            final String advertiserId = channelSegment.getChannelEntity().getAccountId();

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
     *
     * @param currentSegmentsForAdvertiser
     * @param channelSegmentEntity
     * @param maxSegmentSelectionCount
     * @return
     */
    private boolean failedInFilter(final List<ChannelSegment> currentSegmentsForAdvertiser,
        final ChannelSegmentEntity channelSegmentEntity, final int maxSegmentSelectionCount) {
        final SecondaryAdFormatConstraints
            secondaryAdFormatConstraints = channelSegmentEntity.getSecondaryAdFormatConstraints();

        long currentSegmentsWithMatchingDemandConstraints = 0L;
        for (final ChannelSegment channelSegment : currentSegmentsForAdvertiser){
            if (secondaryAdFormatConstraints
                == channelSegment.getChannelSegmentEntity().getSecondaryAdFormatConstraints()) {
                ++currentSegmentsWithMatchingDemandConstraints;
            }
        }
        return currentSegmentsWithMatchingDemandConstraints >= maxSegmentSelectionCount;
    }

    private void incrementStats(final String advertiserId, final int value) {
        ChannelSegment.incrementInspectorStats(advertiserId, InspectorStrings.DROPPED_IN_PARTNER_COUNT_FILTER, value);
    }

    @Override
    final public void setOrder(final FilterOrder order) {
        this.order = order;
    }

    @Override
    public FilterOrder getOrder() {
        return order;
    }

}
