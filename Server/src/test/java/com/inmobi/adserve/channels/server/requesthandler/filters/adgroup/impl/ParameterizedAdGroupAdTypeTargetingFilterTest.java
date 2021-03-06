package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import static com.inmobi.adserve.channels.api.SASParamsUtils.A_PARENTVIEWWIDTH;
import static com.inmobi.adserve.channels.api.SASParamsUtils.isRequestEligibleForMovieBoard;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.segment.impl.AdTypeEnum;


public class ParameterizedAdGroupAdTypeTargetingFilterTest {
    private static AdGroupAdTypeTargetingFilter adGroupAdTypeTargetingFilter;

    @BeforeClass
    public void setUp() {
        adGroupAdTypeTargetingFilter = new AdGroupAdTypeTargetingFilter(null);
    }

    @DataProvider(name = "DataProviderForAdTypeTargetingFilter")
    public Object[][] paramDataProviderForAdTypeTargetingFilter() {
        return new Object[][] {
            {"Banner-PubControlsNull", RequestedAdType.BANNER, SecondaryAdFormatConstraints.STATIC, null, null, false, false, null, false, null, true},
            {"Interstitial-PubControlsNull", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, null, null, false, false, null, false, null, true},
            {"Native-PubControlsNull", RequestedAdType.NATIVE, SecondaryAdFormatConstraints.STATIC, null, null, false, false, null, false, null, true},
            {"Banner-PubControlsAreVideoOnly", RequestedAdType.BANNER, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.VIDEO), null, false, false, null, false, null, true},
            {"Interstitial-PubControlsAreVideoOnly", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.VIDEO), null, false, false, null, false, null, true},
            {"Native-PubControlsAreVideoOnly", RequestedAdType.NATIVE, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.VIDEO), null, false, false, null, false, null, true},
            {"Banner-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, false, null, false, null, false},
            {"Interstitial-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, false, null, false, null, false},
            {"Native-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, false, null, false, null, false},
            {"Banner-Negative-Rewarded", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, true, null, false, null, true},
            {"Interstitial-Negative-Rewarded", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, true, null, false, null, true},
            {"Native-Negative-Rewarded", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, true, null, false, null, true},

            // Video checks take into account that requested ad type must be INTERSTITIAL so video related tests for banner and native are redundant
            {"Interstitial-Vast-SupplyConstraintsDontMatch", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{14L}, false, false, null, false, null, true},
            {"Interstitial-Vast-DemandConstraintsDontMatch", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{13L}, true, false, null, false, null, true},
            {"Interstitial-Vast-Positive14", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{14L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive16", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{16L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive17", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{17L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive31", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{31L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive32", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{32L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive33", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{33L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive34", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{34L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Positive39", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{39L}, true, false, null, false, null, false},
            {"Interstitial-Vast-Negative10", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{10L}, true, false, null, false, null, true},
            {"Interstitial-Vast-Negative40", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{40L}, true, false, null, false, null, true},
            {"Interstitial-Vast-RewardedNegative", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{40L}, true, true, null, false, null, true}, // Cannot show normal Vast on Rewarded
            {"Interstitial-Rewarded-Negative", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO, null, new Long[]{14L}, true, false, null, false, null, true},
            {"Interstitial-Rewarded-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO, null, new Long[]{14L}, true, true, null, false, null, false},
            {"Vast-Out-Video-positive", RequestedAdType.VAST, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, null, false, null, false},
            {"Vast-Out-Video-negative", RequestedAdType.VAST, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, false, false, null, false, null, true},
            {"Vast-Out-Video-negative-isrewarded", RequestedAdType.VAST, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, true, null, false, null, true},
            {"Vast-Out-Video-with-any-slot-id", RequestedAdType.VAST, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{9L}, true, false, null, false, null, false},
            {"Vast-Out-Video-with-mismatch-secondaryAdFormatConstraint-id", RequestedAdType.VAST, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{14L}, true, false, null, false, null, true},
            {"Vast-Out-Video-with-mismatch-secondaryAdFormatConstraint-id", RequestedAdType.VAST, SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO, null, new Long[]{14L}, true, false, null, false, null, true},
            {"Vast-Out-Video-with-mismatch-secondaryAdFormatConstraint-id", RequestedAdType.VAST, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), new Long[]{14L}, true, false, null, false, null, true},
            {"Vast-Out-Video-with-mismatch-secondaryAdFormatConstraint-id", RequestedAdType.VAST, SecondaryAdFormatConstraints.STATIC, null, new Long[]{14L}, true, false, null, false, null, true},
            {"Vast-Out-Video-with-mismatch-requestedAdType-id", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, null, false, null, true},
            {"Vast-Out-Video-with-mismatch-requestedAdType-id", RequestedAdType.NATIVE, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, null, false, null, true},
            {"Vast-Out-Video-with-mismatch-requestedAdType-id", RequestedAdType.BANNER, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, null, false, null, true},

            // MovieBoard Test Cases
            {"MovieBoard-Positive", RequestedAdType.INLINE_BANNER, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, ImmutableSet.of(117), true, "APP", false},
            {"MovieBoard-Negative-No-Capibilities", RequestedAdType.INLINE_BANNER, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, new HashSet<>(), true, "APP", true},
            {"MovieBoard-Negative-Rewarded-Video", RequestedAdType.INLINE_BANNER, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, true, ImmutableSet.of(17), true, "APP", true},
            {"MovieBoard-Negative-Video-Not-Supported", RequestedAdType.INLINE_BANNER, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, false, false, ImmutableSet.of(117), true, "APP", true},
            {"MovieBoard-Negative-NoJs-Tracking-False", RequestedAdType.INLINE_BANNER, SecondaryAdFormatConstraints.PURE_VAST, null, new Long[]{14L}, true, false, ImmutableSet.of(117), false, "APP", true},

        };
    }

    @Test(dataProvider = "DataProviderForAdTypeTargetingFilter")
    public void verifyAdTypeTargetingFilterScenarios(final String testCaseName, final RequestedAdType requestedAdType, final SecondaryAdFormatConstraints secondaryAdFormatConstraints, final
        List<AdTypeEnum> pubControlSupportedAdTypes, final Long[] slotIds, final boolean isVideoSupported, final boolean isRewardedVideo, final Set<Integer> supplyCapabilitiesSet,
                                                     final boolean noJsTracking, final String source, final Boolean expectedReturnValue) throws Exception {

        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setRequestedAdType(requestedAdType);
        sasParams.setPubControlSupportedAdTypes(pubControlSupportedAdTypes);
        sasParams.setVideoSupported(isVideoSupported);
        sasParams.setRewardedVideo(isRewardedVideo);
        sasParams.setSupplyCapabilities(supplyCapabilitiesSet);
        sasParams.setNoJsTracking(noJsTracking);
        sasParams.setSource(source);
        sasParams.setAdPoolParamsMap(ImmutableMap.of(A_PARENTVIEWWIDTH, "100"));
        sasParams.setMovieBoardRequest(isRequestEligibleForMovieBoard(sasParams));

        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        expect(mockChannelSegmentEntity.getSlotIds()).andReturn(slotIds).anyTimes();
        expect(mockChannelSegmentEntity.getId()).andReturn(StringUtils.EMPTY).anyTimes();
        expect(mockChannelSegmentEntity.getSecondaryAdFormatConstraints()).andReturn(secondaryAdFormatConstraints).anyTimes();
        replay(mockChannelSegmentEntity);

        ChannelSegment channelSegment = new ChannelSegment(mockChannelSegmentEntity, null, null, null, null, null, 0.0);

        assertThat(adGroupAdTypeTargetingFilter.failedInFilter(channelSegment, sasParams, null), is(equalTo(expectedReturnValue)));
    }
}
