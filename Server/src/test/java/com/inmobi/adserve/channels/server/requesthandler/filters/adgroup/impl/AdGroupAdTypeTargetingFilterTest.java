package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.resetAll;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.demand.enums.DemandAdFormatConstraints;
import com.inmobi.segment.impl.AdTypeEnum;

/**
 * Created by ishan.bhatnagar on 8/31/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AdGroupAdTypeTargetingFilter.class, InspectorStats.class})
public class AdGroupAdTypeTargetingFilterTest {
    private static AdGroupAdTypeTargetingFilter adGroupAdTypeTargetingFilter;

    @BeforeClass
    public static void setUp() {
        adGroupAdTypeTargetingFilter = new AdGroupAdTypeTargetingFilter(null);
    }

    @Test
    public void testPassedInFilterAsNonInterstitialRequest() throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.BANNER).anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(null, mockSASRequestParameters, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterStaticInterstitialRequestWithNullPubControlSupportedAdTypes() throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSASRequestParameters.getPubControlSupportedAdTypes()).andReturn(null).anyTimes();

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.STATIC)
            .anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterStaticInterstitialRequestWithOnlyVideoInPubControlSupportedAdTypes()
        throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSASRequestParameters.getPubControlSupportedAdTypes()).andReturn(ImmutableList.of(AdTypeEnum.VIDEO))
            .anyTimes();

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.STATIC)
            .anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(true)));
    }

    @Test
    public void testPassedInFilterStaticInterstitialRequest()
        throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSASRequestParameters.getPubControlSupportedAdTypes()).andReturn(ImmutableList.of(AdTypeEnum.BANNER))
            .anyTimes();

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.STATIC)
            .anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterVastVideoRequestAsInvalidVideoSupplyConstraints()
        throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSASRequestParameters.isVideoSupported()).andReturn(false)
            .anyTimes();

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.VAST_VIDEO);
        expect(mockChannelSegmentEntity.getSlotIds()).andReturn(new Long[]{14l})
            .anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterVastVideoRequestAsInvalidVideoDemandConstraints()
        throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSASRequestParameters.isVideoSupported()).andReturn(true)
            .anyTimes();

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.VAST_VIDEO);
        expect(mockChannelSegmentEntity.getSlotIds()).andReturn(new Long[]{15l})
            .anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(true)));
    }

    @Test
    public void testPassedInFilterVastVideoRequest()
        throws Exception {
        resetAll();
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSASRequestParameters.isVideoSupported()).andReturn(true)
            .anyTimes();

        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.VAST_VIDEO);
        expect(mockChannelSegmentEntity.getSlotIds()).andReturn(new Long[]{14l})
            .anyTimes();
        replayAll();

        assertThat(adGroupAdTypeTargetingFilter
            .failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(false)));
    }


}
