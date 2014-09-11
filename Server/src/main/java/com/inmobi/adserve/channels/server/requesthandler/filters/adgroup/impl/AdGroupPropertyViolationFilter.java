package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import javax.inject.Inject;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupPropertyViolationFilter extends AbstractAdGroupLevelFilter {

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupPropertyViolationFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.droppedInPropertyViolationFilter);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();

        if (channelSegmentEntity.isUdIdRequired()
                && ((StringUtils.isEmpty(sasParams.getUidParams()) || sasParams.getUidParams().equals("{}"))
                && (null == sasParams.getTUidParams() || sasParams.getTUidParams().isEmpty()))) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInUdidFilter);
            return true;
        }
        if (channelSegmentEntity.isZipCodeRequired() && sasParams.getPostalCode() == null) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInZipcodeFilter);
            return true;
        }
        if (channelSegmentEntity.isLatlongRequired() && StringUtils.isEmpty(sasParams.getLatLong())) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInLatLongFilter);
            return true;
        }
        if (channelSegmentEntity.isRestrictedToRichMediaOnly() && !sasParams.isRichMedia()) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInRichMediaFilter);
            return true;
        }
        if (channelSegmentEntity.isInterstitialOnly()
                && (sasParams.getRqAdType() == null || !sasParams.getRqAdType().equals("int"))) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInOnlyInterstitialFilter);
            return true;
        }
        if (channelSegmentEntity.isNonInterstitialOnly() && sasParams.getRqAdType() != null
                && sasParams.getRqAdType().equals("int")) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInOnlyNonInterstitialFilter);
            return true;
        }

        return false;
    }

    @Override
    protected void incrementStats(final ChannelSegment channelSegment) {
        // No-op
    }

}
