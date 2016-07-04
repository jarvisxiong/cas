package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.resetAll;

import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStats;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AdGroupAutomationFrameworkFilter.class, CasConfigUtil.class, InspectorStats.class})
public class AdGroupAutomationFrameworkFilterTest {
    private static AdGroupAutomationFrameworkFilter adGroupAutomationFrameworkFilter;

    @BeforeClass
    public static void setUp() {
        adGroupAutomationFrameworkFilter = new AdGroupAutomationFrameworkFilter(null);
    }

    @Test
    public void testFailedInFilterNotAnAutomationRequest() throws Exception {
        resetAll();
        mockStatic(CasConfigUtil.class);
        Configuration mockConfig = createMock(Configuration.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupId()).andReturn("").anyTimes();
        expect(mockChannelSegmentEntity.getAdvertiserId()).andReturn("").anyTimes();
        expect(mockChannelSegmentEntity.getAutomationTestId()).andReturn(null).anyTimes();
        expect(mockConfig.getBoolean("enableAutomationTests", false)).andReturn(true).anyTimes();
        expect(mockSASRequestParameters.getAutomationTestId()).andReturn(null).anyTimes();

        replayAll();

        assertThat(adGroupAutomationFrameworkFilter.failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(false)));
    }

    @Test
    public void testFailedInFilterAutomationTestIdsMismatch() throws Exception {
        resetAll();
        mockStatic(CasConfigUtil.class);
        Configuration mockConfig = createMock(Configuration.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(mockConfig.getBoolean("enableAutomationTests", false)).andReturn(true).anyTimes();
        expect(mockSASRequestParameters.getAutomationTestId()).andReturn("Test#123").anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAutomationTestId()).andReturn("Test#124").anyTimes();

        replayAll();

        assertThat(adGroupAutomationFrameworkFilter.failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(true)));
    }

    @Test
    public void testFailedInFilterAutomationTestIdsMatch() throws Exception {
        resetAll();
        mockStatic(CasConfigUtil.class);
        Configuration mockConfig = createMock(Configuration.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        ChannelSegment mockChannelSegment = createMock(ChannelSegment.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(mockConfig.getBoolean("enableAutomationTests", false)).andReturn(true).anyTimes();
        expect(mockSASRequestParameters.getAutomationTestId()).andReturn("Test#123").anyTimes();
        expect(mockChannelSegment.getChannelSegmentEntity()).andReturn(mockChannelSegmentEntity).anyTimes();
        expect(mockChannelSegmentEntity.getAutomationTestId()).andReturn("Test#123").anyTimes();

        replayAll();

        assertThat(adGroupAutomationFrameworkFilter.failedInFilter(mockChannelSegment, mockSASRequestParameters, null), is(equalTo(false)));
    }
}