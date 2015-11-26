package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.segment.impl.AdTypeEnum;

/**
 * Created by ishan.bhatnagar on 8/31/15.
 */
public class ParameterizedAdGroupAdTypeTargetingFilterTest {
    private static AdGroupAdTypeTargetingFilter adGroupAdTypeTargetingFilter;

    @BeforeClass
    public void setUp() {
        adGroupAdTypeTargetingFilter = new AdGroupAdTypeTargetingFilter(null);
    }

    @DataProvider(name = "DataProviderForAdTypeTargetingFilter")
    public Object[][] paramDataProviderForAdTypeTargetingFilter() {
        return new Object[][] {
            {"Banner-PubControlsNull", RequestedAdType.BANNER, SecondaryAdFormatConstraints.STATIC, null, null, false, false, true},
            {"Interstitial-PubControlsNull", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, null, null, false, false, true},
            {"Native-PubControlsNull", RequestedAdType.NATIVE, SecondaryAdFormatConstraints.STATIC, null, null, false, false, true},
            {"Banner-PubControlsAreVideoOnly", RequestedAdType.BANNER, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.VIDEO), null, false, false, true},
            {"Interstitial-PubControlsAreVideoOnly", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.VIDEO), null, false, false, true},
            {"Native-PubControlsAreVideoOnly", RequestedAdType.NATIVE, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.VIDEO), null, false, false, true},
            {"Banner-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, false, false},
            {"Interstitial-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, false, false},
            {"Native-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, false, false},
            {"Banner-Negative-Rewarded", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, true, true},
            {"Interstitial-Negative-Rewarded", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, true, true},
            {"Native-Negative-Rewarded", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.STATIC, Arrays.asList(AdTypeEnum.BANNER), null, false, true, true},

            // Video checks take into account that requested ad type must be INTERSTITIAL so video related tests for banner and native are redundant
            {"Interstitial-Vast-SupplyConstraintsDontMatch", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{14L}, false, false, true},
            {"Interstitial-Vast-DemandConstraintsDontMatch", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{13L}, true, false, true},
            {"Interstitial-Vast-Positive14", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{14L}, true, false, false},
            {"Interstitial-Vast-Positive16", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{16L}, true, false, false},
            {"Interstitial-Vast-Positive17", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{17L}, true, false, false},
            {"Interstitial-Vast-Positive31", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{31L}, true, false, false},
            {"Interstitial-Vast-Positive32", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{32L}, true, false, false},
            {"Interstitial-Vast-Positive33", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{33L}, true, false, false},
            {"Interstitial-Vast-Positive34", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{34L}, true, false, false},
            {"Interstitial-Vast-Positive39", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{39L}, true, false, false},
            {"Interstitial-Vast-Negative10", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{10L}, true, false, true},
            {"Interstitial-Vast-Negative40", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{40L}, true, false, true},
            {"Interstitial-Vast-RewardedNegative", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.VAST_VIDEO, null, new Long[]{40L}, true, true, true}, // Cannot show normal Vast on Rewarded
            {"Interstitial-Rewarded-Negative", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO, null, new Long[]{14L}, true, false, true},
            {"Interstitial-Rewarded-Positive", RequestedAdType.INTERSTITIAL, SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO, null, new Long[]{14L}, true, true, false},
        };
    }

    @Test(dataProvider = "DataProviderForAdTypeTargetingFilter")
    public void verifyAdTypeTargetingFilterScenarios(final String testCaseName, final RequestedAdType requestedAdType, final SecondaryAdFormatConstraints secondaryAdFormatConstraints, final
        List<AdTypeEnum> pubControlSupportedAdTypes, final Long[] slotIds, final boolean isVideoSupported, final boolean isRewardedVideo, final Boolean expectedReturnValue) throws Exception {

        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setRequestedAdType(requestedAdType);
        sasParams.setPubControlSupportedAdTypes(pubControlSupportedAdTypes);
        sasParams.setVideoSupported(isVideoSupported);
        sasParams.setRewardedVideo(isRewardedVideo);

        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        expect(mockChannelSegmentEntity.getSlotIds()).andReturn(slotIds).anyTimes();
        expect(mockChannelSegmentEntity.getSecondaryAdFormatConstraints()).andReturn(secondaryAdFormatConstraints).anyTimes();
        replay(mockChannelSegmentEntity);

        ChannelSegment channelSegment = new ChannelSegment(mockChannelSegmentEntity, null, null, null, null, null, 0.0);

        assertThat(adGroupAdTypeTargetingFilter.failedInFilter(channelSegment, sasParams, null), is(equalTo(expectedReturnValue)));
    }
}
