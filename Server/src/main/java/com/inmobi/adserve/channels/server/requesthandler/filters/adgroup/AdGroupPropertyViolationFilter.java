package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdGroupPropertyViolationFilter extends AbstractAdGroupLevelFilter {

    /**
     * @param traceMarkerProvider
     */
    protected AdGroupPropertyViolationFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.droppedInImpressionFilter);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();

        if (channelSegmentEntity.isUdIdRequired()
                && (StringUtils.isEmpty(sasParams.getUidParams()) || sasParams.getUidParams().equals("{}"))) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInUdidFilter);
            return true;
        }
        if (channelSegmentEntity.isZipCodeRequired() && StringUtils.isEmpty(sasParams.getPostalCode())) {
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
