package com.inmobi.adserve.channels.server.requesthandler.filters;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdGroupHandsetTargetingFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupHandsetTargetingFilter.class);

    /**
     * @param traceMarkerProvider
     */
    protected AdGroupHandsetTargetingFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.droppedinHandsetTargetingFilter);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        List<Integer> manufModelTargetingList = channelSegment.getChannelSegmentEntity().getManufModelTargetingList();
        return CollectionUtils.isNotEmpty(manufModelTargetingList)
                && !manufModelTargetingList.contains(sasParams.getModelId());
    }

}
