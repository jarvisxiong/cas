package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupHandsetTargetingFilter extends AbstractAdGroupLevelFilter {

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupHandsetTargetingFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_HANDSET_TARGETING_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        final List<Long> manufModelTargetingList =
                channelSegment.getChannelSegmentEntity().getManufModelTargetingList();
        return CollectionUtils.isNotEmpty(manufModelTargetingList)
                && !manufModelTargetingList.contains(sasParams.getModelId());
    }

}
