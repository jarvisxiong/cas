package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.DemandSourceType;


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

    @Override
    public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
            final CasContext casContext) {
        final Marker traceMarker = traceMarkerProvider.get();
        final Map<String, List<ChannelSegment>> advertiserSegmentListMap = getAdvertiserSegmentListMap(channelSegments);
        for (final Entry<String, List<ChannelSegment>> entry : advertiserSegmentListMap.entrySet()) {
            final List<ChannelSegment> segmentListForAdvertiser = entry.getValue();
            final String advertiserId = entry.getKey();
            boolean breakFromSlotLoop = false;
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
                final long videoSlotInSegment =
                        checkIfSegmentShortlistForVideo(channelSegmentSlotIdMap.get(channelSegment), sasParams);
                // check if video supported, and pick it with priority
                if (0l != videoSlotInSegment) {
                    final boolean result =
                            failedInFilter(selectedSegmentListForAdvertiser.size(), maxSegmentSelectionCount);
                    if (result) {
                        breakFromSlotLoop = true;
                        break;
                    } else {
                        addChannelSegment(selectedSegmentListForAdvertiser, maxSegmentSelectionCount, channelSegment,
                                iterator, advertiserId, traceMarker, videoSlotInSegment);
                    }
                }
            }
            // Now choose all other ChannelSegment based on order of requested slot
            final int slotListSize = sasParams.getProcessedMkSlot().size();
            for (int i = 0; i < slotListSize && !breakFromSlotLoop; i++) {
                final Long slotIdFromUmp = Long.valueOf(sasParams.getProcessedMkSlot().get(i));
                for (final Iterator<ChannelSegment> iterator = segmentListForAdvertiser.listIterator(); iterator
                        .hasNext();) {
                    final ChannelSegment channelSegment = iterator.next();
                    if (channelSegmentSlotIdMap.get(channelSegment).contains(slotIdFromUmp)) {
                        final boolean result =
                                failedInFilter(selectedSegmentListForAdvertiser.size(), maxSegmentSelectionCount);
                        if (result) {
                            breakFromSlotLoop = true;
                            break;
                        } else {
                            addChannelSegment(selectedSegmentListForAdvertiser, maxSegmentSelectionCount,
                                    channelSegment, iterator, advertiserId, traceMarker, slotIdFromUmp);
                        }
                    }
                }
            }
            LOG.debug(traceMarker, "Number of segments {} failed in filter {}  , advertiser {}",
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
        incrementTotalSelectedSegmentStats(channelSegment);
        // Removing channel segment since it has already been passed and stored in
        // selectedSegmentListForAdvertiser.
        // We don't want to traverse over it again
        iterator.remove();
    }

    private long checkIfSegmentShortlistForVideo(final List<Long> channelSegmentSlotIdList,
            final SASRequestParameters sasRequestParameters) {

        // If we get multiple slots in the Ad pool request, give preference to video supported slots - 14 & 32.
        if (DemandSourceType.IX.getValue() == sasRequestParameters.getDst()
                && sasRequestParameters.isVideoSupported()) {
            List<Short> processedSlots = sasRequestParameters.getProcessedMkSlot();
            if (channelSegmentSlotIdList.contains(14L) && processedSlots.contains((short) 14)) {
                return 14l;
            } else if (channelSegmentSlotIdList.contains(32L) && processedSlots.contains((short) 32)) {
                return 32l;
            }
        }
        return 0;
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
     * @param segmentCountForAdvertiser
     * @param maxSegmentSelectionCount
     * @return boolean
     */
    private boolean failedInFilter(final int segmentCountForAdvertiser, final int maxSegmentSelectionCount) {
        return segmentCountForAdvertiser >= maxSegmentSelectionCount;
    }

    private void incrementStats(final String advertiserId, final int value) {
        ChannelSegment.incrementInspectorStats(advertiserId, InspectorStrings.DROPPED_IN_PARTNER_COUNT_FILTER, value);
    }

    /**
     * @param channelSegment
     */
    private void incrementTotalSelectedSegmentStats(final ChannelSegment channelSegment) {
        channelSegment.incrementInspectorStats(InspectorStrings.TOTAL_SELECTED_SEGMENTS);
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
