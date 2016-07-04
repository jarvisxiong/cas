package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


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
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_PROPERTY_VIOLATION_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
        if (channelSegmentEntity.isUdIdRequired()
                && (null == sasParams.getTUidParams() || sasParams.getTUidParams().isEmpty())) {
            channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_UDID_FILTER);
            return true;
        }
        if (channelSegmentEntity.isZipCodeRequired() && sasParams.getPostalCode() == null) {
            channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_ZIPCODE_FILTER);
            return true;
        }
        if (channelSegmentEntity.isLatlongRequired() && StringUtils.isEmpty(sasParams.getLatLong())) {
            channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_LAT_LONG_FILTER);
            return true;
        }
        if (channelSegmentEntity.isRestrictedToRichMediaOnly() && !sasParams.isRichMedia()) {
            channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_RICH_MEDIA_FILTER);
            return true;
        }

        final RequestedAdType requestedAdType = sasParams.getRequestedAdType();
        if (channelSegmentEntity.isInterstitialOnly()
                && (null == requestedAdType || RequestedAdType.INTERSTITIAL != requestedAdType)) {
            channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_ONLY_INTERSTITIAL_FILTER);
            return true;
        }
        if (channelSegmentEntity.isNonInterstitialOnly() && null != requestedAdType
                && RequestedAdType.INTERSTITIAL == requestedAdType) {
            channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_ONLY_NON_INTERSTITIAL_FILTER);
            return true;
        }
        return false;
    }

    @Override
    protected void incrementStats(final ChannelSegment channelSegment) {
        // No-op
    }

}
