package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.collect.ImmutableList;
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
    // Only interstitial slots 10 and 40 are not supported.
    private static final List<Long> slotsSupportedByVASTTemplates =
        ImmutableList.of(14L, 16L, 17L, 31L, 32L, 33L, 34L, 39L);

    @Inject
    protected AdGroupAdTypeTargetingFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_AD_TYPE_TARGETING_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
        final CasContext casContext) {

        final boolean returnValue;
        final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
        switch (channelSegmentEntity.getSecondaryAdFormatConstraints()) {
            case VAST_VIDEO:
                returnValue =
                    sasParams.isRewardedVideo() || RequestedAdType.VAST == sasParams.getRequestedAdType() ||
                            !checkVideoEligibility(channelSegment.getChannelSegmentEntity().getSlotIds(), sasParams);
                break;
            case PURE_VAST:
                returnValue = sasParams.isRewardedVideo() || RequestedAdType.VAST != sasParams.getRequestedAdType()
                        || !checkVideoEligibility(channelSegment.getChannelSegmentEntity().getSlotIds(), sasParams);
                break;
            case REWARDED_VAST_VIDEO:
                returnValue =
                    !(sasParams.isRewardedVideo() && RequestedAdType.VAST != sasParams.getRequestedAdType() &&
                            checkVideoEligibility(channelSegment.getChannelSegmentEntity().getSlotIds(), sasParams));
                break;
            case STATIC:
                // Not enforcing interstitial slots. Assuming that this is correctly handled in UMP.
                final List<AdTypeEnum> supportedAdTypes = sasParams.getPubControlSupportedAdTypes();
                returnValue =
                    CollectionUtils.isEmpty(supportedAdTypes) || RequestedAdType.VAST == sasParams.getRequestedAdType()
                            || !supportedAdTypes.contains(AdTypeEnum.BANNER) || sasParams.isRewardedVideo();
                break;
            default:
                InspectorStats.incrementStatCount(InspectorStrings.DROPPED_AS_UNKNOWN_ADGROUP_AD_TYPE);
                LOG.info("Dropped as unknown demand constraint was encountered");
                returnValue = true;
        }
        return returnValue;
    }


    private boolean checkVideoEligibility(final Long[] channelSegmentSlotIds, final SASRequestParameters sasParams) {
        final boolean videoSupplyConstraintsMatch = sasParams.isVideoSupported();
        final boolean videoDemandConstraintsMatch =
            CollectionUtils.containsAny(Arrays.asList(channelSegmentSlotIds), slotsSupportedByVASTTemplates);

        return (videoSupplyConstraintsMatch && videoDemandConstraintsMatch);
    }
}
