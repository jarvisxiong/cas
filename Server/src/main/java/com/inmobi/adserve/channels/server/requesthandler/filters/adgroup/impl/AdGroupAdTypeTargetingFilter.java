package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.segment.impl.AdTypeEnum;

/**
 * Created by ishan.bhatnagar on 8/31/15.
 */
@Singleton
public final class AdGroupAdTypeTargetingFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupAdTypeTargetingFilter.class);

    @Inject
    protected AdGroupAdTypeTargetingFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_AD_TYPE_TARGETING_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
        final CasContext casContext) {

        boolean returnValue = false;

        if (RequestedAdType.INTERSTITIAL == sasParams.getRequestedAdType()) {
            final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
            switch (channelSegmentEntity.getDemandAdFormatConstraints()) {
                case VAST_VIDEO:
                    final boolean videoSupplyConstraintsMatch = sasParams.isVideoSupported();
                    final boolean videoDemandConstraintsMatch = checkVideoDemandConstraints(
                        Arrays.asList(channelSegment.getChannelSegmentEntity().getSlotIds()));

                    returnValue = !(videoSupplyConstraintsMatch && videoDemandConstraintsMatch) ;
                    break;
                case STATIC:
                    // Not enforcing interstitial slots. Assuming that this is correctly handled in UMP.
                    final List<AdTypeEnum> supportedAdTypes = sasParams.getPubControlSupportedAdTypes();
                    returnValue =
                        CollectionUtils.isEmpty(supportedAdTypes) || !supportedAdTypes.contains(AdTypeEnum.BANNER);
                    break;
                default:
                    InspectorStats.incrementStatCount(InspectorStrings.DROPPED_AS_UNKNOWN_ADGROUP_AD_TYPE);
                    LOG.info("Dropped as unknown demand constraint was encountered");
                    returnValue = true;
            }
        }
        return returnValue;
    }

    // TODO: Move to config
    private boolean checkVideoDemandConstraints(final List<Long> channelSegmentSlotIds) {
        return channelSegmentSlotIds.contains(14L) || channelSegmentSlotIds.contains(32L);
    }
}
