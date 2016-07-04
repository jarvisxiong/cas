package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;

/**
 * Created by ishanbhatnagar on 28/1/15.
 */
@Singleton
public class AdGroupAutomationFrameworkFilter extends AbstractAdGroupLevelFilter {

    @Inject
    protected AdGroupAutomationFrameworkFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_AUTOMATION_FRAMEWORK_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        final String automationTestId = sasParams.getAutomationTestId();
        final String segmentAutomationId = channelSegment.getChannelSegmentEntity().getAutomationTestId();

        return !(StringUtils.isBlank(automationTestId) ||
                 (StringUtils.isNotBlank(segmentAutomationId) && segmentAutomationId.contains(automationTestId)));
    }
}
